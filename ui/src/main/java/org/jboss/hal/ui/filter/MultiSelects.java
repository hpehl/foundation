package org.jboss.hal.ui.filter;

import java.util.List;
import java.util.function.Function;

import org.patternfly.component.menu.MenuItem;
import org.patternfly.filter.Filter;
import org.patternfly.filter.FilterAttribute;

import static java.util.stream.Collectors.toList;

class MultiSelects {

    static <T> void setBooleanFilter(Filter<T> filter, String filterAttribute, List<MenuItem> menuItems, String origin) {
        String prefix = filterAttribute + "-";
        List<MenuItem> selected = menuItems.stream()
                .filter(menuItem -> menuItem.identifier().startsWith(prefix))
                .collect(toList());
        if (selected.isEmpty()) {
            filter.reset(filterAttribute, origin);
        } else {
            for (MenuItem menuItem : selected) {
                String selection = menuItem.identifier().substring(prefix.length());
                filter.set(filterAttribute, Boolean.parseBoolean(selection), origin);
            }
        }
    }

    static <T, V> void collectIdentifiers(List<String> identifiers, Filter<T> filter, String filterAttribute,
            Function<V, String> valueToIdentifier) {
        if (filter.defined(filterAttribute)) {
            FilterAttribute<T, V> value = filter.get(filterAttribute);
            identifiers.add(valueToIdentifier.apply(value.value()));
        }
    }
}
