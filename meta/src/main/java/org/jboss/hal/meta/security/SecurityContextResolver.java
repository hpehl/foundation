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
package org.jboss.hal.meta.security;

import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Placeholder;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.meta.SegmentResolver;
import org.jboss.hal.meta.StatementContext;

import static org.jboss.hal.meta.Placeholder.DOMAIN_CONTROLLER;
import static org.jboss.hal.meta.Placeholder.SELECTED_HOST;
import static org.jboss.hal.meta.Placeholder.SELECTED_SERVER;
import static org.jboss.hal.meta.Placeholder.SELECTED_SERVER_GROUP;

/**
 * A segment resolver that resolves all placeholders, but {@link Placeholder#DOMAIN_CONTROLLER}, {@link Placeholder#SELECTED_HOST}, {@link Placeholder#SELECTED_SERVER_GROUP} and {@link Placeholder#SELECTED_SERVER} with wildcards ({@code *}).
 * <pre>
 * / → /
 * /subsystem=io → subsystem=io
 * {selected.server} → server=foo
 * {selected.server}/deployment=bar → server=foo/deployment=bar
 * subsystem=logging/logger={selection} → subsystem=logging/logger=*
 * </pre>
 */
class SecurityContextResolver implements SegmentResolver {

    private final Environment environment;

    SecurityContextResolver(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Segment resolve(StatementContext context, AddressTemplate template,
            Segment segment, boolean first, boolean last, int index) {
        if (segment.containsPlaceholder()) {
            if (segment.hasKey()) {
                return new Segment(segment.key, "*");
            } else {
                if (environment.standalone()) {
                    return context.resolve(segment);
                } else {
                    Placeholder placeholder = segment.placeholder();
                    if (DOMAIN_CONTROLLER.equals(placeholder) ||
                            SELECTED_HOST.equals(placeholder) ||
                            SELECTED_SERVER_GROUP.equals(placeholder) ||
                            SELECTED_SERVER.equals(placeholder)) {
                        return context.resolve(segment);
                    } else {
                        return new Segment(segment.key, "*");
                    }
                }
            }
        }
        return segment;
    }
}
