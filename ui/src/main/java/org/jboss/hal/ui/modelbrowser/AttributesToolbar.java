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
package org.jboss.hal.ui.modelbrowser;

import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.filter.AccessTypeFilterAttribute;
import org.jboss.hal.ui.filter.DeprecatedFilterAttribute;
import org.jboss.hal.ui.filter.FilterChips;
import org.jboss.hal.ui.filter.StorageFilterAttribute;
import org.jboss.hal.ui.filter.TypesFilterAttribute;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.filter.DeprecatedFilterMultiSelect.deprecatedFilterMultiSelect;
import static org.jboss.hal.ui.filter.ModeFilterMultiSelect.modeFilterMultiSelect;
import static org.jboss.hal.ui.filter.NameFilterTextInputGroup.nameFilterTextInputGroup;
import static org.jboss.hal.ui.filter.TypesFilterMultiSelect.typesFilterMultiSelect;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarFilterChipGroup.toolbarFilterChipGroup;
import static org.patternfly.component.toolbar.ToolbarFilterContent.toolbarFilterContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.filterGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.toolbar.ToolbarItemType.searchFilter;

class AttributesToolbar implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static AttributesToolbar attributesToolbar(Filter<AttributeDescription> filter) {
        return new AttributesToolbar(filter);
    }

    // ------------------------------------------------------ instance

    private final Toolbar toolbar;

    private AttributesToolbar(Filter<AttributeDescription> filter) {
        toolbar = toolbar()
                .addContent(toolbarContent()
                        .addItem(toolbarItem(searchFilter).add(nameFilterTextInputGroup(filter)))
                        .addGroup(toolbarGroup(filterGroup)
                                .addItem(toolbarItem().add(typesFilterMultiSelect(filter)))
                                .addItem(toolbarItem().add(deprecatedFilterMultiSelect(filter, "Status")))
                                .addItem(toolbarItem().add(modeFilterMultiSelect(filter)))))
                .addFilterContent(toolbarFilterContent()
                        .bindVisibility(filter, TypesFilterAttribute.NAME, DeprecatedFilterAttribute.NAME,
                                StorageFilterAttribute.NAME, AccessTypeFilterAttribute.NAME)
                        .addGroup(toolbarGroup()
                                .add(toolbarFilterChipGroup(filter, "Type")
                                        .filterAttributes(TypesFilterAttribute.NAME)
                                        .filterToChips(FilterChips::typeChips))
                                .add(toolbarFilterChipGroup(filter, "Status")
                                        .filterAttributes(DeprecatedFilterAttribute.NAME)
                                        .filterToChips(FilterChips::deprecatedChips))
                                .add(toolbarFilterChipGroup(filter, "Mode")
                                        .filterAttributes(StorageFilterAttribute.NAME, AccessTypeFilterAttribute.NAME)
                                        .filterToChips(FilterChips::modeChips)))
                        .addItem(toolbarItem()
                                .add(button("Clear all filters").link().inline().onClick((e, c) -> filter.resetAll()))));
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }
}
