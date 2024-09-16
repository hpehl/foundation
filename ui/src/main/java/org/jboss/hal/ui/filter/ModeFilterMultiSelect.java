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
package org.jboss.hal.ui.filter;

import java.util.ArrayList;
import java.util.List;

import org.jboss.elemento.IsElement;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MultiSelect;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuGroup.menuGroup;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MultiSelect.multiSelect;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectGroupMenu;
import static org.patternfly.icon.IconSets.fas.filter;

public class ModeFilterMultiSelect<T> implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static <T> ModeFilterMultiSelect<T> modeFilterMultiSelect(Filter<T> filter) {
        return new ModeFilterMultiSelect<>(filter);
    }

    // ------------------------------------------------------ instance

    private static final String ORIGIN = "ModeFilterMultiSelect";
    private final MultiSelect multiSelect;

    ModeFilterMultiSelect(Filter<T> filter) {
        filter.onChange(this::onFilterChanged);
        this.multiSelect = multiSelect(menuToggle().icon(filter()).text("Mode"))
                .stayOpen()
                .addMenu(multiSelectGroupMenu()
                        .onMultiSelect((e, c, menuItems) -> {
                            changeFilter(filter, menuItems, StorageFilterAttribute.NAME);
                            changeFilter(filter, menuItems, AccessTypeFilterAttribute.NAME);
                        })
                        .addContent(menuContent()
                                .addGroup(menuGroup("Storage")
                                        .addList(menuList()
                                                .addItem(StorageFilterAttribute.NAME + "-configuration", "Configuration")
                                                .addItem(StorageFilterAttribute.NAME + "-runtime", "Runtime")))
                                .addDivider()
                                .addGroup(menuGroup("Access type")
                                        .addList(menuList()
                                                .addItem(AccessTypeFilterAttribute.NAME + "-read-write", "Read-write")
                                                .addItem(AccessTypeFilterAttribute.NAME + "-read-only", "Read-only")
                                                .addItem(AccessTypeFilterAttribute.NAME + "-metric", "Metric")))));
    }

    @Override
    public HTMLElement element() {
        return multiSelect.element();
    }

    // ------------------------------------------------------ internal

    private void changeFilter(Filter<T> filter, List<MenuItem> menuItems, String name) {
        String prefix = name + "-";
        List<MenuItem> selected = menuItems.stream()
                .filter(menuItem -> menuItem.identifier().startsWith(prefix))
                .collect(toList());
        if (selected.isEmpty()) {
            filter.reset(name, ORIGIN);
        } else {
            for (MenuItem menuItem : selected) {
                String selection = menuItem.identifier().substring(prefix.length());
                filter.set(name, selection, ORIGIN);
            }
        }
    }

    private void onFilterChanged(Filter<T> filter, String origin) {
        if (!origin.equals(ORIGIN)) {
            multiSelect.clear(false);
            List<String> selectIds = new ArrayList<>();
            if (filter.defined(StorageFilterAttribute.NAME)) {
                String value = filter.<String>get(StorageFilterAttribute.NAME).value();
                selectIds.add(StorageFilterAttribute.NAME + "-" + value);
            }
            if (filter.defined(AccessTypeFilterAttribute.NAME)) {
                String value = filter.<String>get(AccessTypeFilterAttribute.NAME).value();
                selectIds.add(AccessTypeFilterAttribute.NAME + "-" + value);
            }
            multiSelect.selectIds(selectIds, false);
        }
    }
}
