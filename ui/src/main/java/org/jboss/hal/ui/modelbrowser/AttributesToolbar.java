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
import java.util.function.Consumer;

import org.jboss.elemento.IsElement;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.filter.AccessTypeFilterValue;
import org.jboss.hal.meta.filter.DeprecatedFilterValue;
import org.jboss.hal.meta.filter.NameFilterValue;
import org.jboss.hal.meta.filter.StorageFilterValue;
import org.jboss.hal.meta.filter.TypesFilterValue;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MultiSelect;
import org.patternfly.component.menu.SingleSelect;
import org.patternfly.component.textinputgroup.TextInputGroup;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.icon.IconSets.fas;

import elemental2.dom.HTMLElement;

import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.singletonList;
import static org.patternfly.component.badge.Badge.badge;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.checkboxMenuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MultiSelect.multiSelect;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectMenu;
import static org.patternfly.component.menu.SingleSelect.singleSelect;
import static org.patternfly.component.menu.SingleSelectMenu.singleSelectMenu;
import static org.patternfly.component.textinputgroup.TextInputGroup.searchInputGroup;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.style.Classes.modifier;

class AttributesToolbar implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static AttributesToolbar attributesToolbar() {
        return new AttributesToolbar();
    }

    // ------------------------------------------------------ instance

    private static final String MODEL_TYPE_KEY = "modelType";
    private final AttributesFilter filter;
    private final Toolbar toolbar;
    private final TextInputGroup filterByName;
    private final MultiSelect filterByType;
    private final SingleSelect filterByDeprecated;
    private final SingleSelect filterByStorage;
    private final SingleSelect filterByAccessType;
    private Consumer<AttributesFilter> onFilter;

    private AttributesToolbar() {
        filter = new AttributesFilter();
        filterByName = searchInputGroup("Filter by name")
                .onChange((event, textInputGroup, value) -> {
                    filter.set(NameFilterValue.NAME, value);
                    filter();
                });
        filterByType = multiSelect(menuToggle("Types")
                .icon(fas.filter())
                .addBadge(badge(0).read()))
                .addMenu(multiSelectMenu()
                        .onMultiSelect((event, menuItem, selected) -> {
                            if (selected.isEmpty()) {
                                filter.reset(TypesFilterValue.NAME);
                            } else {
                                List<ModelType> modelTypes = new ArrayList<>();
                                for (MenuItem item : selected) {
                                    List<ModelType> types = item.get(MODEL_TYPE_KEY);
                                    modelTypes.addAll(types);
                                }
                                filter.set(TypesFilterValue.NAME, modelTypes);
                            }
                            filter();
                        })
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
        filterByDeprecated = singleSelect(menuToggle().icon(fas.filter()))
                .addMenu(singleSelectMenu()
                        .onSingleSelect((event, menuItem, selected) -> {
                            if ("all".equals(menuItem.identifier())) {
                                filter.reset(DeprecatedFilterValue.NAME);
                            } else {
                                filter.set(DeprecatedFilterValue.NAME, parseBoolean(menuItem.identifier()));
                            }
                            filter();
                        })
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItem("all", "All deprecated")
                                        .addItem("true", "Deprecated")
                                        .addItem("false", "Not deprecated"))));
        filterByStorage = singleSelect(menuToggle()
                .icon(fas.filter())
                .text("All storage"))
                .addMenu(singleSelectMenu()
                        .onSingleSelect((event, menuItem, selected) -> {
                            if ("all".equals(menuItem.identifier())) {
                                filter.reset(StorageFilterValue.NAME);
                            } else {
                                filter.set(StorageFilterValue.NAME, menuItem.identifier());
                            }
                            filter();
                        })
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItem("all", "All storage")
                                        .addItem("configuration", "Configuration")
                                        .addItem("runtime", "Runtime"))));
        filterByAccessType = singleSelect(menuToggle()
                .icon(fas.filter())
                .text("All access type"))
                .addMenu(singleSelectMenu()
                        .onSingleSelect((event, menuItem, selected) -> {
                            if ("all".equals(menuItem.identifier())) {
                                filter.reset(AccessTypeFilterValue.NAME);
                            } else {
                                filter.set(AccessTypeFilterValue.NAME, menuItem.identifier());
                            }
                            filter();
                        })
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItem("all", "All access type")
                                        .addItem("read-write", "Read-write")
                                        .addItem("read-only", "Read-only")
                                        .addItem("metric", "Metric"))));
        toolbar = toolbar()
                .addContent(toolbarContent()
                        .addItem(toolbarItem().css(modifier("search-filter"))
                                .add(filterByName))
                        .addGroup(toolbarGroup().css(modifier("filter-group"))
                                .addItem(toolbarItem().add(filterByType))
                                .addItem(toolbarItem().add(filterByDeprecated))
                                .addItem(toolbarItem().add(filterByStorage))
                                .addItem(toolbarItem().add(filterByAccessType))));
        clearFilter();
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }

    // ------------------------------------------------------ events

    AttributesToolbar onFilter(Consumer<AttributesFilter> onFilter) {
        this.onFilter = onFilter;
        return this;
    }

    // ------------------------------------------------------ api

    void clearFilter() {
        filter.resetAll();
        filterByName.clear(false);
        filterByType.clear();
        filterByDeprecated.select("all", false);
        filterByStorage.select("all", false);
        filterByAccessType.select("all", false);
    }

    // ------------------------------------------------------ internal

    private void filter() {
        if (onFilter != null) {
            onFilter.accept(filter);
        }
    }
}
