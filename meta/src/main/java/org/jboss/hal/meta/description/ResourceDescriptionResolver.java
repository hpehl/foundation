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
package org.jboss.hal.meta.description;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Placeholder;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.meta.TemplateResolver;

import static org.jboss.hal.meta.Placeholder.DOMAIN_CONTROLLER;
import static org.jboss.hal.meta.Placeholder.SELECTED_RESOURCE;

/**
 * A segment resolver that resolves all placeholders and the value of the last segment with wildcards ({@code *}).
 * <pre>
 * / → /
 * /subsystem=io → subsystem=io
 * {selected.server} → server=*
 * {selected.server}/deployment=foo → server=*&#47;deployment=*
 * subsystem=logging/logger={selection} → subsystem=logging/logger=*
 * </pre>
 */
class ResourceDescriptionResolver implements TemplateResolver {

    @Override
    public AddressTemplate resolve(AddressTemplate template) {
        List<Segment> resolved = new ArrayList<>();
        for (Iterator<Segment> iterator = template.iterator(); iterator.hasNext(); ) {
            Segment segment = iterator.next();
            // use wildcards where possible
            if (segment.containsPlaceholder()) {
                Placeholder placeholder = segment.placeholder();
                if (SELECTED_RESOURCE.equals(placeholder) ||
                        (template.size() == 1 &&
                                DOMAIN_CONTROLLER.equals(segment.placeholder()) ||
                                SELECTED_RESOURCE.equals(placeholder))) {
                    resolved.add(new Segment(segment.key, "*"));
                }
                if (segment.hasKey()) {
                    resolved.add(new Segment(segment.key, "*"));
                } else {
                    Placeholder placeholder = segment.placeholder();
                    resolved.add(new Segment(placeholder.resource, "*"));
                }
            } else if (!iterator.hasNext() && !"*".equals(segment.value)) {
                resolved.add(new Segment(segment.key, "*"));
            } else {
                resolved.add(segment);
            }
        }
        return AddressTemplate.of(resolved);
    }
}
