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

public class FilterAttribute {

    /**
     * The filter name must apply to element dataset rules
     *
     * @see <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/dataset#name_conversion">https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/dataset#name_conversion</a>
     */
    public final String name;
    private final String initialValue;
    private final FilterCondition condition;
    private String value;
    private boolean defined;

    public FilterAttribute(String name, String initialValue, FilterCondition condition) {
        this.name = name;
        this.initialValue = initialValue;
        this.condition = condition;
        set(initialValue);
    }

    public boolean isDefined() {
        return defined;
    }

    public boolean matches(String attributeValue) {
        return condition.matches(attributeValue, this.value);
    }

    void set(String value) {
        this.value = value;
        this.defined = !this.value.equals(initialValue);
    }

    void reset() {
        set(initialValue);
    }

    @Override
    public String toString() {
        return name + "=" + (defined ? value : initialValue + "<initial>");
    }
}
