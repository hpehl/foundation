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

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StatementContext {

    private final Map<Placeholder, String> values;

    StatementContext() {
        values = new HashMap<>();
    }

    public Segment resolve(Segment segment) {
        if (segment.containsPlaceholder()) {
            Placeholder placeholder = segment.placeholder();
            String resolvedValue = value(placeholder);
            if (resolvedValue != null) {
                if (segment.hasKey()) {
                    // key={placeholder}
                    return new Segment(segment.key, resolvedValue);
                } else {
                    // {placeholder}
                    return new Segment(placeholder.resource, resolvedValue);
                }
            } else {
                throw new ResolveException("No value found for placeholder " + placeholder.name + " in segment " + segment);
            }
        }
        return segment;
    }

    public void assign(String placeholder, String value) {
        values.put(new Placeholder(placeholder), value);
    }

    public String value(Placeholder placeHolder) {
        return values.get(placeHolder);
    }
}
