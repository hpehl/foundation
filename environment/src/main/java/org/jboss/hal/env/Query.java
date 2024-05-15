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
package org.jboss.hal.env;

import elemental2.dom.URLSearchParams;

import static elemental2.dom.DomGlobal.location;

public final class Query {

    public static boolean hasParameter(String name) {
        if (name != null && !location.search.isEmpty()) {
            URLSearchParams query = new URLSearchParams(location.search);
            return query.has(name);
        }
        return false;
    }

    public static String getParameter(String name) {
        if (hasParameter(name)) {
            URLSearchParams query = new URLSearchParams(location.search);
            if (query.has(name)) {
                String value = query.get(name);
                return value.isEmpty() ? null : value;
            }
        }
        return null;
    }
}
