/*
 *  Copyright 2024 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ui.resource;

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.ui.filter.AccessTypeFilterAttribute;
import org.jboss.hal.ui.filter.DefinedFilterAttribute;
import org.jboss.hal.ui.filter.DeprecatedFilterAttribute;
import org.jboss.hal.ui.filter.NameFilterAttribute;
import org.jboss.hal.ui.filter.StorageFilterAttribute;
import org.patternfly.component.chip.ChipGroup;
import org.patternfly.component.textinputgroup.TextInputGroup;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.component.toolbar.ToolbarFilterContent;
import org.patternfly.component.toolbar.ToolbarGroup;
import org.patternfly.component.toolbar.ToolbarGroupType;
import org.patternfly.component.toolbar.ToolbarItem;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.ui.filter.ModeFilterMultiSelect.modeFilterMultiSelect;
import static org.jboss.hal.ui.filter.StatusFilterMultiSelect.statusFilterMultiSelect;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.chip.Chip.chip;
import static org.patternfly.component.chip.ChipGroup.chipGroup;
import static org.patternfly.component.textinputgroup.TextInputGroup.searchInputGroup;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarFilterContent.toolbarFilterContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.iconButtonGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.toolbar.ToolbarItemType.chipGroup;
import static org.patternfly.component.toolbar.ToolbarItemType.searchFilter;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.icon.IconSets.fas.edit;
import static org.patternfly.icon.IconSets.fas.link;
import static org.patternfly.icon.IconSets.fas.undo;
import static org.patternfly.popper.Placement.auto;
import static org.patternfly.style.Classes.modifier;

class ResourceToolbar implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static ResourceToolbar resourceToolbar(Filter<ResourceAttribute> filter) {
        return new ResourceToolbar(filter);
    }

    // ------------------------------------------------------ instance

    private final Filter<ResourceAttribute> filter;
    private final Toolbar toolbar;
    private final TextInputGroup filterByName;
    private ToolbarFilterContent filterContent;
    private ToolbarGroup filterGroup;
    private ToolbarItem statusToolbarItem;
    private ToolbarItem modeToolbarItem;
    private ChipGroup statusChipGroup;
    private ChipGroup modeChipGroup;

    private ResourceToolbar(Filter<ResourceAttribute> filter) {
        this.filter = filter;
        this.filter.onChange(this::onFilterChanged);

        String resolveId = Id.unique("resolve-expressions");
        String resetId = Id.unique("reset");
        String editId = Id.unique("edit");

        filterByName = searchInputGroup("Filter by name")
                .onChange((event, textInputGroup, value) -> filter.set(NameFilterAttribute.NAME, value));
        toolbar = toolbar()
                .addContent(toolbarContent()
                        .addItem(toolbarItem().type(searchFilter)
                                .add(filterByName))
                        .addGroup(toolbarGroup().type(ToolbarGroupType.filterGroup)
                                .addItem(toolbarItem().add(statusFilterMultiSelect(filter)))
                                .addItem(toolbarItem().add(modeFilterMultiSelect(filter))))
                        .addGroup(toolbarGroup().type(iconButtonGroup).css(modifier("align-right"))
                                .addItem(toolbarItem()
                                        .add(button().id(resolveId).link().icon(link()))
                                        .add(tooltip(By.id(resolveId), "Resolve all expressions")
                                                .placement(auto)))
                                .addItem(toolbarItem()
                                        .add(button().id(resetId).link().icon(undo()))
                                        .add(tooltip(By.id(resetId),
                                                "Reset attributes to their initial or default value. Applied only to nillable attributes without relationships to other attributes.")
                                                .placement(auto)))
                                .addItem(toolbarItem()
                                        .add(button().id(editId).link().icon(edit()))
                                        .add(tooltip(By.id(editId), "Edit resource")
                                                .placement(auto)))));
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }

    // ------------------------------------------------------ internal

    private void onFilterChanged(Filter<ResourceAttribute> filter, String origin) {
        if (filter.defined()) {
            boolean definedFa = filter.defined(DefinedFilterAttribute.NAME);
            boolean deprecatedFa = filter.defined(DeprecatedFilterAttribute.NAME);
            boolean storageFa = filter.defined(StorageFilterAttribute.NAME);
            boolean accessFa = filter.defined(AccessTypeFilterAttribute.NAME);

            if (definedFa || deprecatedFa) {
                failSafeStatusChipGroup().clear();
                if (definedFa) {
                    Boolean value = filter.<Boolean>get(DefinedFilterAttribute.NAME).value();
                    failSafeStatusChipGroup().addItem(chip(value ? "defined" : "undefined")
                            .onClose((event, chip) -> filter.reset(DefinedFilterAttribute.NAME)));
                }
                if (deprecatedFa) {
                    Boolean value = filter.<Boolean>get(DeprecatedFilterAttribute.NAME).value();
                    failSafeStatusChipGroup().addItem(chip(value ? "deprecated" : "not deprecated")
                            .onClose((event, chip) -> filter.reset(DeprecatedFilterAttribute.NAME)));
                }
            } else {
                failSafeRemoveFromParent(statusToolbarItem);
                statusToolbarItem = null;
                statusChipGroup = null;
            }
            if (storageFa || accessFa) {
                failSafeModeChipGroup().clear();
                if (storageFa) {
                    String value = filter.<String>get(StorageFilterAttribute.NAME).value();
                    failSafeModeChipGroup().addItem(chip(value)
                            .onClose((event, chip) -> filter.reset(StorageFilterAttribute.NAME)));
                }
                if (accessFa) {
                    String value = filter.<String>get(AccessTypeFilterAttribute.NAME).value();
                    failSafeModeChipGroup().addItem(chip(value)
                            .onClose((event, chip) -> filter.reset(AccessTypeFilterAttribute.NAME)));
                }
            } else {
                failSafeRemoveFromParent(modeToolbarItem);
                modeToolbarItem = null;
                modeChipGroup = null;
            }
        } else {
            filterByName.clear(false);
            failSafeRemoveFromParent(filterContent);
            filterContent = null;
            filterGroup = null;
            statusToolbarItem = null;
            statusChipGroup = null;
            modeToolbarItem = null;
            modeChipGroup = null;
        }
    }

    private void failSafeToolbarFilterContent() {
        if (filterContent == null) {
            toolbar.addFilterContent(filterContent = toolbarFilterContent()
                    .addGroup(filterGroup = toolbarGroup())
                    .addItem(toolbarItem()
                            .add(button("Clear all filters").link().inline()
                                    .onClick((event, component) -> filter.resetAll()))));
        }
    }

    private ChipGroup failSafeStatusChipGroup() {
        if (filterGroup == null) {
            failSafeToolbarFilterContent();
        }
        if (statusChipGroup == null) {
            filterGroup.addItem(statusToolbarItem = toolbarItem().type(chipGroup)
                    .add(statusChipGroup = chipGroup("Status")
                            .closable((event, component) -> {
                                filter.reset(DefinedFilterAttribute.NAME);
                                filter.reset(DeprecatedFilterAttribute.NAME);
                            })));
            if (modeToolbarItem != null) {
                // status should be first
                modeToolbarItem.element().before(statusToolbarItem.element());
            }
        }
        return statusChipGroup;
    }

    private ChipGroup failSafeModeChipGroup() {
        if (filterGroup == null) {
            failSafeToolbarFilterContent();
        }
        if (modeChipGroup == null) {
            filterGroup.addItem(modeToolbarItem = toolbarItem().type(chipGroup)
                    .add(modeChipGroup = chipGroup("Mode")
                            .closable((event, component) -> {
                                filter.reset(StorageFilterAttribute.NAME);
                                filter.reset(AccessTypeFilterAttribute.NAME);
                            })));
        }
        return modeChipGroup;
    }
}
