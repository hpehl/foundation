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
package org.jboss.hal.model.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class Filter implements Iterable<FilterAttribute> {

    public static final String DATA = "filter";

    public static boolean asBoolean(String value, boolean defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public static int asInteger(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private final Map<String, FilterAttribute> attributes;

    protected Filter(List<FilterAttribute> attributes) {
        this.attributes = new HashMap<>();
        for (FilterAttribute attribute : attributes) {
            this.attributes.put(attribute.name, attribute);
        }
    }

    @Override
    public Iterator<FilterAttribute> iterator() {
        return attributes.values().iterator();
    }

    public boolean isDefined() {
        return attributes.values().stream().anyMatch(FilterAttribute::isDefined);
    }

    public void set(String name, String value) {
        FilterAttribute filterValue = attributes.get(name);
        if (filterValue != null) {
            filterValue.set(value);
        }
    }

    public void reset(String name) {
        FilterAttribute filterValue = attributes.get(name);
        if (filterValue != null) {
            filterValue.reset();
        }
    }

    public void resetAll() {
        attributes.values().forEach(FilterAttribute::reset);
    }

    @Override
    public String toString() {
        return attributes.values().toString();
    }
}
