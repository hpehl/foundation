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
package org.jboss.hal.meta.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;

public abstract class Filter<T> implements Iterable<FilterValue<T, ?>> {

    // ------------------------------------------------------ instance

    private static final String FILTER_VALUE_SEPARATOR = "|";
    private static final String FILTER_VALUE_SEPARATOR_REGEX = "\\|";
    private static final String NAME_VALUE_SEPARATOR = "=";
    private final Map<String, FilterValue<T, ?>> attributes;

    protected Filter() {
        this.attributes = new HashMap<>();
    }

    @Override
    public String toString() {
        return attributes.values().toString();
    }

    // ------------------------------------------------------ api

    @Override
    public Iterator<FilterValue<T, ?>> iterator() {
        return attributes.values().iterator();
    }

    public boolean filter(T object) {
        boolean filtered = false;
        for (Iterator<FilterValue<T, ?>> iterator = this.iterator(); iterator.hasNext() && !filtered; ) {
            FilterValue<T, ?> filterValue = iterator.next();
            if (filterValue.defined) {
                filtered = !filterValue.matches(object);
            }
        }
        return filtered;
    }

    public boolean defined() {
        return attributes.values().stream().anyMatch(filterValue -> filterValue.defined);
    }

    @SuppressWarnings("unchecked")
    public <V> void set(String name, V value) {
        FilterValue<T, ?> filterValue = attributes.get(name);
        if (filterValue != null) {
            FilterValue<T, V> tv = (FilterValue<T, V>) filterValue;
            tv.set(value);
        }
    }

    public void reset(String name) {
        FilterValue<T, ?> filterValue = attributes.get(name);
        if (filterValue != null) {
            filterValue.reset();
        }
    }

    public void resetAll() {
        attributes.values().forEach(FilterValue::reset);
    }

    public String save() {
        return stream(spliterator(), false)
                .filter(filterValue -> filterValue.persistent && filterValue.defined)
                .map(filterValue -> filterValue.name + NAME_VALUE_SEPARATOR + filterValue.save())
                .collect(joining(FILTER_VALUE_SEPARATOR));
    }

    public void load(String filter) {
        if (filter != null) {
            String[] parts = filter.split(FILTER_VALUE_SEPARATOR_REGEX);
            for (String part : parts) {
                if (part != null && part.contains(NAME_VALUE_SEPARATOR)) {
                    String[] nv = part.split(NAME_VALUE_SEPARATOR, 2);
                    FilterValue<T, ?> filterValue = attributes.get(nv[0]);
                    if (filterValue != null) {
                        filterValue.load(nv[1]);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------ internal

    protected <V> void add(FilterValue<T, V> attribute) {
        attributes.put(attribute.name, attribute);
    }
}
