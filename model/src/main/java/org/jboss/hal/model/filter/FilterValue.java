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

import java.util.Objects;

public class FilterValue<T, V> {

    final String name;
    private final V initialValue;
    private final FilterCondition<T, V> condition;
    private V value;
    private boolean defined;

    public FilterValue(String name, V initialValue, FilterCondition<T, V> condition) {
        this.name = name;
        this.initialValue = initialValue;
        this.condition = condition;
        set(initialValue);
    }

    public boolean isDefined() {
        return defined;
    }

    public boolean matches(T object) {
        return condition.matches(object, this.value);
    }

    void set(V value) {
        this.value = value;
        this.defined = !Objects.equals(initialValue, this.value);
    }

    void reset() {
        set(initialValue);
    }

    @Override
    public String toString() {
        return name + "=" + (defined ? value : initialValue + "<initial>");
    }
}
