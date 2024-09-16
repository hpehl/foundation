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
import java.util.function.Function;

import org.jboss.elemento.IsElement;
import org.patternfly.component.chip.Chip;
import org.patternfly.component.chip.ChipGroup;
import org.patternfly.component.toolbar.ToolbarItem;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static java.util.Collections.emptyList;
import static org.jboss.elemento.Elements.setVisible;
import static org.patternfly.component.chip.ChipGroup.chipGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;

public class ToolbarFilterChipGroup<T> implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static <T> ToolbarFilterChipGroup<T> toolbarFilterChipGroup(Filter<T> filter, String text) {
        return new ToolbarFilterChipGroup<>(filter, text);
    }

    // ------------------------------------------------------ instance

    private final List<String> filterAttributes;
    private final ChipGroup chipGroup;
    private final ToolbarItem toolbarItem;
    private Function<Filter<T>, List<Chip>> chipsFn;

    ToolbarFilterChipGroup(Filter<T> filter, String text) {
        this.filterAttributes = new ArrayList<>();
        this.chipsFn = f -> emptyList();
        this.toolbarItem = toolbarItem()
                .add(chipGroup = chipGroup(text).closable((e, c) -> filterAttributes.forEach(filter::reset)));

        setVisible(this, false);
        filter.onChange((f, origin) -> {
            chipGroup.clear();
            setVisible(this, filterAttributes.stream().anyMatch(f::defined));
            if (filterAttributes.stream().anyMatch(f::defined)) {
                chipsFn.apply(f).forEach(chipGroup::add);
            }
        });
    }

    @Override
    public HTMLElement element() {
        return toolbarItem.element();
    }

    // ------------------------------------------------------ builder

    public ToolbarFilterChipGroup<T> filterAttributes(String firstAttribute, String... moreAttributes) {
        filterAttributes.add(firstAttribute);
        filterAttributes.addAll(List.of(moreAttributes));
        return this;
    }

    public ToolbarFilterChipGroup<T> filterToChips(Function<Filter<T>, List<Chip>> chipsFn) {
        this.chipsFn = chipsFn;
        return this;
    }
}
