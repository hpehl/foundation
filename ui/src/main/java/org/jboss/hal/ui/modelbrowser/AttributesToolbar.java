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
import org.jboss.hal.ui.filter.NameFilterAttribute;
import org.patternfly.component.textinputgroup.TextInputGroup;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.filter.DeprecatedFilterMultiSelect.deprecatedFilterMultiSelect;
import static org.jboss.hal.ui.filter.ModeFilterMultiSelect.modeFilterMultiSelect;
import static org.jboss.hal.ui.filter.TypesFilterMultiSelect.typesFilterMultiSelect;
import static org.patternfly.component.textinputgroup.TextInputGroup.searchInputGroup;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
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

    private final Filter<AttributeDescription> filter;
    private final Toolbar toolbar;
    private final TextInputGroup filterByName;

    private AttributesToolbar(Filter<AttributeDescription> filter) {
        this.filter = filter;
        this.filter.onChange(this::onFilterChanged);

        filterByName = searchInputGroup("Filter by name")
                .onChange((event, textInputGroup, value) -> filter.set(NameFilterAttribute.NAME, value));
        toolbar = toolbar()
                .addContent(toolbarContent()
                        .addItem(toolbarItem().type(searchFilter)
                                .add(filterByName))
                        .addGroup(toolbarGroup().type(filterGroup)
                                .addItem(toolbarItem().add(typesFilterMultiSelect(filter)))
                                .addItem(toolbarItem().add(deprecatedFilterMultiSelect("Status", filter)))
                                .addItem(toolbarItem().add(modeFilterMultiSelect(filter)))));
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }

    // ------------------------------------------------------ internal

    private void onFilterChanged(Filter<AttributeDescription> filter, String origin) {
        if (filter.defined()) {
            // TODO Show filter toolbar
        } else {
            filterByName.clear(false);
        }
    }
}
