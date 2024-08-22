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

import java.util.function.Consumer;

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.patternfly.component.menu.SingleSelect;
import org.patternfly.component.textinputgroup.TextInputGroup;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.icon.IconSets.fas;

import elemental2.dom.HTMLElement;

import static java.lang.Boolean.parseBoolean;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.SingleSelect.singleSelect;
import static org.patternfly.component.menu.SingleSelectMenu.singleSelectMenu;
import static org.patternfly.component.textinputgroup.TextInputGroup.searchInputGroup;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.icon.IconSets.fas.edit;
import static org.patternfly.icon.IconSets.fas.link;
import static org.patternfly.icon.IconSets.fas.undo;
import static org.patternfly.popper.Placement.auto;
import static org.patternfly.style.Classes.modifier;

class ResourceToolbar implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static ResourceToolbar resourceToolbar() {
        return new ResourceToolbar();
    }

    // ------------------------------------------------------ instance

    private final ResourceFilter filter;
    private final Toolbar toolbar;
    private final TextInputGroup filterByName;
    private final SingleSelect filterByDefined;
    private final SingleSelect filterByDeprecated;
    private final SingleSelect filterByStorage;
    private final SingleSelect filterByAccessType;
    private Consumer<ResourceFilter> onFilter;

    private ResourceToolbar() {
        String resolveId = Id.unique("resolve-expressions");
        String resetId = Id.unique("reset");
        String editId = Id.unique("edit");

        filter = new ResourceFilter();
        filterByName = searchInputGroup("Filter by name")
                .onChange((event, textInputGroup, value) -> {
                    filter.set(ResourceFilter.NAME_FILTER, value);
                    filter();
                });
        filterByDefined = singleSelect(menuToggle().icon(fas.filter()))
                .addMenu(singleSelectMenu()
                        .onSingleSelect((event, menuItem, selected) -> {
                            if ("all".equals(menuItem.identifier())) {
                                filter.reset(ResourceFilter.UNDEFINED_FILTER);
                            } else {
                                filter.set(ResourceFilter.UNDEFINED_FILTER,
                                        parseBoolean(menuItem.identifier()));
                            }
                            filter();
                        })
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItem("all", "All defined")
                                        .addItem("true", "Defined")
                                        .addItem("false", "Undefined"))));
        filterByDeprecated = singleSelect(menuToggle().icon(fas.filter()))
                .addMenu(singleSelectMenu()
                        .onSingleSelect((event, menuItem, selected) -> {
                            if ("all".equals(menuItem.identifier())) {
                                filter.reset(ResourceFilter.DEPRECATED_FILTER);
                            } else {
                                filter.set(ResourceFilter.DEPRECATED_FILTER,
                                        parseBoolean(menuItem.identifier()));
                            }
                            filter();
                        })
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItem("all", "All deprecated")
                                        .addItem("true", "Deprecated")
                                        .addItem("false", "Not deprecated"))));
        filterByStorage = singleSelect(menuToggle().icon(fas.filter()))
                .addMenu(singleSelectMenu()
                        .onSingleSelect((event, menuItem, selected) -> {
                            if ("all".equals(menuItem.identifier())) {
                                filter.reset(ResourceFilter.STORAGE_FILTER);
                            } else {
                                filter.set(ResourceFilter.STORAGE_FILTER,
                                        menuItem.identifier());
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
                                filter.reset(ResourceFilter.ACCESS_TYPE_FILTER);
                            } else {
                                filter.set(ResourceFilter.ACCESS_TYPE_FILTER,
                                        menuItem.identifier());
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
                        .addItem(toolbarItem().css(modifier("search-filter")).add(filterByName))
                        .addGroup(toolbarGroup().css(modifier("filter-group"))
                                .addItem(toolbarItem().add(filterByDefined))
                                .addItem(toolbarItem().add(filterByDeprecated))
                                .addItem(toolbarItem().add(filterByStorage))
                                .addItem(toolbarItem().add(filterByAccessType)))
                        .addGroup(toolbarGroup().css(modifier("icon-button-group"), modifier("align-right"))
                                .addItem(toolbarItem()
                                        .add(button().id(resolveId).link().icon(link()))
                                        .add(tooltip(By.id(resolveId), "Resolve all expressions")
                                                .placement(auto)))
                                .addItem(toolbarItem()
                                        .add(button().id(resetId).link().icon(undo()))
                                        .add(tooltip(By.id(resetId),
                                                "Resets attributes to their initial or default value. Applied only to nillable attributes without relationships to other attributes.")
                                                .placement(auto)))
                                .addItem(toolbarItem()
                                        .add(button().id(editId).link().icon(edit()))
                                        .add(tooltip(By.id(editId), "Edit resource")
                                                .placement(auto)))));
        clearFilter();
    }

    @Override
    public HTMLElement element() {
        return toolbar.element();
    }

    // ------------------------------------------------------ events

    ResourceToolbar onFilter(Consumer<ResourceFilter> onFilter) {
        this.onFilter = onFilter;
        return this;
    }

    // ------------------------------------------------------ api

    void clearFilter() {
        filter.resetAll();
        filterByName.clear(false);
        filterByDefined.select("all", false);
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
