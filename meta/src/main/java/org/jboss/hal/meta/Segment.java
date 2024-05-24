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
package org.jboss.hal.meta;

public class Segment {

    public final String key;
    public final String value;

    Segment(String value) {
        this(null, value);
    }

    public Segment(String key, String value) {
        this.key = key;
        this.value = AddressTemplate.encodeValue(value);
    }

    public boolean hasKey() {
        return key != null;
    }

    public boolean containsPlaceholder() {
        return value != null && value.startsWith("{") && value.endsWith("}");
    }

    public Placeholder placeholder() {
        return containsPlaceholder() ? new Placeholder(value.substring(1, value.length() - 1)) : null;
    }

    @Override
    public String toString() {
        return hasKey() ? key + "=" + value : value;
    }
}
