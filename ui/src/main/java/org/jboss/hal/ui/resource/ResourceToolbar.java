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
import org.jboss.hal.ui.filter.FilterChips;
import org.jboss.hal.ui.filter.StorageFilterAttribute;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.component.toolbar.ToolbarItem;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.filter.ItemCount.itemCount;
import static org.jboss.hal.ui.filter.ModeFilterMultiSelect.modeFilterMultiSelect;
import static org.jboss.hal.ui.filter.NameFilterTextInputGroup.nameFilterTextInputGroup;
import static org.jboss.hal.ui.filter.StatusFilterMultiSelect.statusFilterMultiSelect;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarFilterChipGroup.toolbarFilterChipGroup;
import static org.patternfly.component.toolbar.ToolbarFilterContent.toolbarFilterContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.filterGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.iconButtonGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.toolbar.ToolbarItemType.searchFilter;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.icon.IconSets.fas.edit;
import static org.patternfly.icon.IconSets.fas.link;
import static org.patternfly.icon.IconSets.fas.sync;
import static org.patternfly.icon.IconSets.fas.undo;
import static org.patternfly.popper.Placement.auto;
import static org.patternfly.style.Classes.modifier;

class ResourceToolbar implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static ResourceToolbar resourceToolbar(ResourceView resourceView, Filter<ResourceAttribute> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        return new ResourceToolbar(resourceView, filter, visible, total);
    }

    // ------------------------------------------------------ instance

    private final Toolbar toolbar;

    private ResourceToolbar(ResourceView resourceView, Filter<ResourceAttribute> filter,
            ObservableValue<Integer> visible, ObservableValue<Integer> total) {
        // TODO RBAC
        String resolveId = Id.unique("resolve-expressions");
        String resetId = Id.unique("reset");
        String refreshId = Id.unique("refresh");
        String editId = Id.unique("edit");

        ToolbarItem resolveItem = toolbarItem()
                .add(button().id(resolveId).plain().icon(link()).onClick((e, b) -> resourceView.resolve()))
                .add(tooltip(By.id(resolveId), "Resolve all expressions").placement(auto));
        ToolbarItem resetItem = toolbarItem()
                .add(button().id(resetId).plain().icon(undo()).onClick((e, b) -> resourceView.reset()))
                .add(tooltip(By.id(resetId),
                        "Reset attributes to their initial or default value. Applied only to nillable attributes without relationships to other attributes.")
                        .placement(auto));
        ToolbarItem refreshItem = toolbarItem()
                .add(button().id(refreshId).plain().icon(sync()).onClick((e, b) -> resourceView.refresh()))
                .add(tooltip(By.id(refreshId), "Refresh").placement(auto));
        ToolbarItem editItem = toolbarItem()
                .add(button().id(editId).plain().icon(edit()).onClick((e, b) -> resourceView.edit()))
                .add(tooltip(By.id(editId), "Edit resource").placement(auto));

        toolbar = toolbar()
                .addContent(toolbarContent()
                        .addItem(toolbarItem(searchFilter).add(nameFilterTextInputGroup(filter)))
                        .addGroup(toolbarGroup(filterGroup)
                                .addItem(toolbarItem().add(statusFilterMultiSelect(filter)))
                                .addItem(toolbarItem().add(modeFilterMultiSelect(filter))))
                        .addItem(toolbarItem()
                                .style("align-self", "center")
                                .add(itemCount(visible, total, "attribute", "attributes")))
                        .addGroup(toolbarGroup(iconButtonGroup).css(modifier("align-right"))
                                .addItem(refreshItem)
                                .addItem(resolveItem)
                                .addItem(resetItem)
                                .addItem(editItem)))
                .addFilterContent(toolbarFilterContent()
                        .bindVisibility(filter, DefinedFilterAttribute.NAME, DeprecatedFilterAttribute.NAME,
                                StorageFilterAttribute.NAME, AccessTypeFilterAttribute.NAME)
                        .addGroup(toolbarGroup()
                                .add(toolbarFilterChipGroup(filter, "Status")
                                        .filterAttributes(DefinedFilterAttribute.NAME, DeprecatedFilterAttribute.NAME)
                                        .filterToChips(FilterChips::statusChips))
                                .add(toolbarFilterChipGroup(filter, "Mode")
                                        .filterAttributes(StorageFilterAttribute.NAME, AccessTypeFilterAttribute.NAME)
                                        .filterToChips(FilterChips::modeChips)))
                        .addItem(toolbarItem()
                                .add(button("Clear all filters").link().inline()
                                        .onClick((e, c) -> filter.resetAll()))));
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }
}
