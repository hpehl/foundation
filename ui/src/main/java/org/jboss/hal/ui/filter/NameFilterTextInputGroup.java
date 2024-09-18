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

import org.jboss.elemento.IsElement;
import org.patternfly.component.textinputgroup.TextInputGroup;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.textinputgroup.TextInputGroup.searchInputGroup;

public class NameFilterTextInputGroup<T> implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static <T> NameFilterTextInputGroup<T> nameFilterTextInputGroup(Filter<T> filter) {
        return new NameFilterTextInputGroup<>(filter);
    }

    // ------------------------------------------------------ instance

    private final TextInputGroup textInputGroup;

    NameFilterTextInputGroup(Filter<T> filter) {
        textInputGroup = searchInputGroup("Filter by name")
                .onChange((event, textInputGroup, value) -> filter.set(NameFilterAttribute.NAME, value));
        filter.onChange((f, origin) -> {
            if (!f.defined(NameFilterAttribute.NAME)) {
                textInputGroup.clear(false);
            }
        });
    }

    @Override
    public HTMLElement element() {
        return textInputGroup.element();
    }
}
