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

import java.util.Objects;

public abstract class FilterValue<T, V> {

    // ------------------------------------------------------ instance

    private static final String INITIAL_VALUE = "<initial>";
    protected final String name;
    protected final V initialValue;
    protected final boolean persistent;
    protected final FilterCondition<T, V> condition;
    protected V value;
    protected boolean defined;

    protected FilterValue(String name, V initialValue, boolean persistent, FilterCondition<T, V> condition) {
        this.name = name;
        this.initialValue = initialValue;
        this.persistent = persistent;
        this.condition = condition;
        set(initialValue);
    }

    @Override
    public String toString() {
        return name + "=" + (defined ? value : initialValue + INITIAL_VALUE);
    }

    // ------------------------------------------------------ internal

    protected boolean matches(T object) {
        return condition.matches(object, this.value);
    }

    protected void set(V value) {
        this.value = value;
        this.defined = !Objects.equals(initialValue, this.value);
    }


    protected void reset() {
        set(initialValue);
    }

    protected String save() {
        return String.valueOf(value);
    }

    protected abstract void load(String value);
}
