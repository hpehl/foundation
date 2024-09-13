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

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.filter.NameFilterAttribute;
import org.jboss.hal.ui.filter.TypesFilterAttribute;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MultiSelect;
import org.patternfly.component.textinputgroup.TextInputGroup;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.filter.Filter;
import org.patternfly.icon.IconSets.fas;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static org.jboss.hal.ui.filter.DeprecatedFilterMultiSelect.deprecatedFilterMultiSelect;
import static org.jboss.hal.ui.filter.ModeFilterMultiSelect.modeFilterMultiSelect;
import static org.patternfly.component.badge.Badge.badge;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.checkboxMenuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MultiSelect.multiSelect;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectMenu;
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

    private static final String MODEL_TYPE_KEY = "modelType";
    private final Filter<AttributeDescription> filter;
    private final Toolbar toolbar;
    private final TextInputGroup filterByName;
    private final MultiSelect filterByType;

    private AttributesToolbar(Filter<AttributeDescription> filter) {
        this.filter = filter;
        this.filter.onChange(this::onFilterChanged);

        filterByName = searchInputGroup("Filter by name")
                .onChange((event, textInputGroup, value) -> filter.set(NameFilterAttribute.NAME, value));
        filterByType = multiSelect(menuToggle("Types")
                .icon(fas.filter())
                .addBadge(badge(0).read()))
                .addMenu(multiSelectMenu()
                        .onMultiSelect((event, menuItem, selected) -> filterTypes(selected))
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItem(checkboxMenuItem("Z", "Boolean")
                                                .store(MODEL_TYPE_KEY, singletonList(ModelType.BOOLEAN)))
                                        .addItem(checkboxMenuItem("b", "Bytes")
                                                .store(MODEL_TYPE_KEY, singletonList(ModelType.BYTES)))
                                        .addItem(checkboxMenuItem("e", "Expression")
                                                .store(MODEL_TYPE_KEY, singletonList(ModelType.EXPRESSION)))
                                        .addItem(checkboxMenuItem("I|J|D|i|d", "Numeric")
                                                .store(MODEL_TYPE_KEY, List.of(ModelType.INT,
                                                        ModelType.LONG,
                                                        ModelType.DOUBLE,
                                                        ModelType.BIG_INTEGER,
                                                        ModelType.BIG_DECIMAL)))
                                        .addItem(checkboxMenuItem("l", "List")
                                                .store(MODEL_TYPE_KEY, singletonList(ModelType.LIST)))
                                        .addItem(checkboxMenuItem("o", "Object")
                                                .store(MODEL_TYPE_KEY, singletonList(ModelType.OBJECT)))
                                        .addItem(checkboxMenuItem("p", "Property")
                                                .store(MODEL_TYPE_KEY, singletonList(ModelType.PROPERTY)))
                                        .addItem(checkboxMenuItem("s", "String")
                                                .store(MODEL_TYPE_KEY, singletonList(ModelType.STRING))))));
        toolbar = toolbar()
                .addContent(toolbarContent()
                        .addItem(toolbarItem().type(searchFilter)
                                .add(filterByName))
                        .addGroup(toolbarGroup().type(filterGroup)
                                .addItem(toolbarItem().add(filterByType))
                                .addItem(toolbarItem().add(deprecatedFilterMultiSelect(filter)))
                                .addItem(toolbarItem().add(modeFilterMultiSelect(filter)))));
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }

    // ------------------------------------------------------ internal

    private void filterTypes(List<MenuItem> selected) {
        if (selected.isEmpty()) {
            filter.reset(TypesFilterAttribute.NAME);
        } else {
            List<ModelType> modelTypes = new ArrayList<>();
            for (MenuItem item : selected) {
                List<ModelType> types = item.get(MODEL_TYPE_KEY);
                modelTypes.addAll(types);
            }
            filter.set(TypesFilterAttribute.NAME, modelTypes);
        }
    }

    private void onFilterChanged(Filter<AttributeDescription> filter, String origin) {
        if (filter.defined()) {
            // TODO Show filter toolbar
        } else {
            filterByName.clear(false);
            filterByType.clear(false);
        }
    }
}
