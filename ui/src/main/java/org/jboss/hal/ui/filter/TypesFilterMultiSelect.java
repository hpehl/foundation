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
import static org.jboss.hal.ui.filter.TypeValues.typeValues;
import static org.patternfly.component.badge.Badge.badge;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.checkboxMenuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MultiSelect.multiSelect;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectCheckboxMenu;

public class TypesFilterMultiSelect<T> implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static <T> TypesFilterMultiSelect<T> typesFilterMultiSelect(Filter<T> filter) {
        return new TypesFilterMultiSelect<>(filter);
    }

    // ------------------------------------------------------ instance

    private static final String ORIGIN = "TypesFilterMultiSelect";
    private static final String TYPE_ITEM_KEY = "typeItem";
    private final MultiSelect multiSelect;

    TypesFilterMultiSelect(Filter<T> filter) {
        filter.onChange(this::onFilterChanged);
        this.multiSelect = multiSelect(menuToggle("Type").addBadge(badge(0).read()))
                .addMenu(multiSelectCheckboxMenu()
                        .onMultiSelect((event, menuItem, selected) -> changeFilter(filter, selected))
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItems(typeValues(), tv -> checkboxMenuItem(tv.identifier, tv.name)
                                                .store(TYPE_ITEM_KEY, tv)))));
    }

    @Override
    public HTMLElement element() {
        return multiSelect.element();
    }

    // ------------------------------------------------------ internal

    private void changeFilter(Filter<T> filter, List<MenuItem> selected) {
        if (selected.isEmpty()) {
            filter.reset(TypesFilterAttribute.NAME, ORIGIN);
        } else {
            List<TypeValues> types = new ArrayList<>();
            for (MenuItem item : selected) {
                TypeValues type = item.get(TYPE_ITEM_KEY);
                types.add(type);
            }
            filter.set(TypesFilterAttribute.NAME, types, ORIGIN);
        }
    }

    private void onFilterChanged(Filter<T> filter, String origin) {
        if (!origin.equals(ORIGIN)) {
            multiSelect.clear(false);
            if (filter.defined(TypesFilterAttribute.NAME)) {
                List<String> selectIds = filter.<List<TypeValues>>get(TypesFilterAttribute.NAME).value().stream()
                        .map(tv -> tv.identifier)
                        .collect(toList());
                multiSelect.selectIds(selectIds, false);
            }
        }
    }
}
