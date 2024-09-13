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
import org.patternfly.icon.IconSets;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuGroup.menuGroup;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.MultiSelect.multiSelect;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectGroupMenu;

public class DeprecatedFilterMultiSelect<T> implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static <T> DeprecatedFilterMultiSelect<T> deprecatedFilterMultiSelect(Filter<T> filter) {
        return new DeprecatedFilterMultiSelect<>(filter);
    }

    // ------------------------------------------------------ instance

    private static final String ORIGIN = "DeprecatedFilterMultiSelect";
    private final MultiSelect multiSelect;

    DeprecatedFilterMultiSelect(Filter<T> filter) {
        filter.onChange(this::onFilterChanged);
        this.multiSelect = multiSelect(menuToggle().icon(IconSets.fas.filter()).text("Deprecated"))
                .stayOpen()
                .addMenu(multiSelectGroupMenu()
                        .onMultiSelect((e, c, menuItems) -> setFilterAttributeValue(filter, menuItems))
                        .addContent(menuContent()
                                .addGroup(menuGroup()
                                        .addList(menuList()
                                                .addItem(DeprecatedFilterAttribute.NAME + "-true", "Deprecated")
                                                .addItem(DeprecatedFilterAttribute.NAME + "-false", "Not deprecated")))));
    }

    @Override
    public HTMLElement element() {
        return multiSelect.element();
    }

    // ------------------------------------------------------ internal

    private void setFilterAttributeValue(Filter<T> filter, List<MenuItem> menuItems) {
        String prefix = DeprecatedFilterAttribute.NAME + "-";
        List<MenuItem> selected = menuItems.stream()
                .filter(menuItem -> menuItem.identifier().startsWith(prefix))
                .collect(toList());
        if (selected.isEmpty()) {
            filter.reset(DeprecatedFilterAttribute.NAME, ORIGIN);
        } else {
            for (MenuItem menuItem : selected) {
                String selection = menuItem.identifier().substring(prefix.length());
                filter.set(DeprecatedFilterAttribute.NAME, Boolean.parseBoolean(selection), ORIGIN);
            }
        }
    }

    private void onFilterChanged(Filter<T> filter, String origin) {
        if (!ORIGIN.equals(origin)) {
            multiSelect.clear(false);
            if (filter.defined()) {
                List<String> selectIds = new ArrayList<>();
                boolean deprecatedFa = filter.defined(DeprecatedFilterAttribute.NAME);
                if (deprecatedFa) {
                    boolean value = filter.<Boolean>get(DeprecatedFilterAttribute.NAME).value();
                    selectIds.add(DeprecatedFilterAttribute.NAME + "-" + value);
                }
                multiSelect.selectIds(selectIds, false);
            }
        }
    }
}
