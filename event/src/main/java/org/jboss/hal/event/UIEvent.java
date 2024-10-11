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
package org.jboss.hal.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Marker interface for HAL UI events. Implementations should provide static factory methods to create and return instances of
 * {@link elemental2.dom.CustomEvent}s.
 *
 * @see <a
 * href="https://developer.mozilla.org/en-US/docs/Web/Events/Creating_and_triggering_events">https://developer.mozilla.org/en-US/docs/Web/Events/Creating_and_triggering_events</a>
 */
public interface UIEvent {

    static String type(String identifier, String... identifiers) {
        List<String> allIdentifiers = new ArrayList<>();
        allIdentifiers.add("hal");
        allIdentifiers.add(identifier);
        allIdentifiers.addAll(List.of(identifiers));
        return String.join("::", allIdentifiers);
    }
}
