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
import org.jboss.hal.dmr.ModelType;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MultiSelect;
import org.patternfly.filter.Filter;
import org.patternfly.icon.IconSets;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.patternfly.component.badge.Badge.badge;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.checkboxMenuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MultiSelect.multiSelect;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectMenu;

public class TypesFilterMultiSelect<T> implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static <T> TypesFilterMultiSelect<T> typesFilterMultiSelect(Filter<T> filter) {
        return new TypesFilterMultiSelect<>(filter);
    }

    // ------------------------------------------------------ instance

    private static final String ORIGIN = "TypesFilterMultiSelect";
    private static final String MODEL_TYPE_KEY = "modelType";
    private final MultiSelect multiSelect;

    TypesFilterMultiSelect(Filter<T> filter) {
        filter.onChange(this::onFilterChanged);
        this.multiSelect = multiSelect(menuToggle("Types")
                .icon(IconSets.fas.filter())
                .addBadge(badge(0).read()))
                .addMenu(multiSelectMenu()
                        .onMultiSelect((event, menuItem, selected) -> changeFilter(filter, selected))
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
            List<ModelType> modelTypes = new ArrayList<>();
            for (MenuItem item : selected) {
                List<ModelType> types = item.get(MODEL_TYPE_KEY);
                modelTypes.addAll(types);
            }
            filter.set(TypesFilterAttribute.NAME, modelTypes, ORIGIN);
        }
    }

    private void onFilterChanged(Filter<T> filter, String origin) {
        if (!origin.equals(ORIGIN)) {
            multiSelect.clear(false);
            if (filter.defined(TypesFilterAttribute.NAME)) {
                List<String> selectIds = filter.<List<ModelType>>get(TypesFilterAttribute.NAME).value().stream()
                        .map(type -> String.valueOf(type.getTypeChar()))
                        .collect(toList());
                multiSelect.selectIds(selectIds, false);
            }
        }
    }
}
