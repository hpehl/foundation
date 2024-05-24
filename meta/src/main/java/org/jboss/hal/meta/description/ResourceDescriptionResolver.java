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

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Placeholder;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.meta.SegmentResolver;
import org.jboss.hal.meta.StatementContext;

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
class ResourceDescriptionResolver implements SegmentResolver {

    @Override
    public Segment resolve(StatementContext context, AddressTemplate template,
            Segment segment, boolean first, boolean last, int index) {
        // use wildcards where possible
        if (segment.containsPlaceholder()) {
            if (segment.hasKey()) {
                return new Segment(segment.key, "*");
            } else {
                Placeholder placeholder = segment.placeholder();
                return new Segment(placeholder.resource, "*");
            }
        } else if (last && !"*".equals(segment.value)) {
            return new Segment(segment.key, "*");
        }
        return segment;
    }
}