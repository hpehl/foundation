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
