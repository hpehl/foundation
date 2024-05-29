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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Placeholder;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.meta.TemplateResolver;

import static org.jboss.hal.meta.Placeholder.DOMAIN_CONTROLLER;
import static org.jboss.hal.meta.Placeholder.SELECTED_HOST;
import static org.jboss.hal.meta.Placeholder.SELECTED_SERVER;
import static org.jboss.hal.meta.Placeholder.SELECTED_SERVER_GROUP;

/**
 * A segment resolver that resolves all placeholders, but {@link Placeholder#DOMAIN_CONTROLLER},
 * {@link Placeholder#SELECTED_HOST}, {@link Placeholder#SELECTED_SERVER_GROUP} and {@link Placeholder#SELECTED_SERVER} with
 * wildcards ({@code *}).
 * <pre>
 * / → /
 * /subsystem=io → subsystem=io
 * {selected.host} → {selected.host}
 * {selected.server}/deployment=bar → {selected.server}/deployment=bar
 * subsystem=logging/logger={selection} → subsystem=logging/logger=*
 * {selected.profile}/subsystem=logging/logger={selection} → profile=*&#47;subsystem=logging/logger=*
 * </pre>
 */
class SecurityContextResolver implements TemplateResolver {

    private static final Set<Placeholder> PRESERVE = new HashSet<>();

    static {
        PRESERVE.add(DOMAIN_CONTROLLER);
        PRESERVE.add(SELECTED_HOST);
        PRESERVE.add(SELECTED_SERVER);
        PRESERVE.add(SELECTED_SERVER_GROUP);
    }

    private final Environment environment;

    SecurityContextResolver(Environment environment) {
        this.environment = environment;
    }

    @Override
    public AddressTemplate resolve(AddressTemplate template) {
        List<Segment> resolved = new ArrayList<>();
        for (Segment segment : template) {
            if (segment.containsPlaceholder()) {
                if (segment.hasKey()) {
                    resolved.add(new Segment(segment.key, "*"));
                } else {
                    if (!environment.standalone()) {
                        Placeholder placeholder = segment.placeholder();
                        if (!PRESERVE.contains(placeholder)) {
                            resolved.add(new Segment(segment.key, "*"));
                        }
                    }
                }
            } else {
                resolved.add(segment);
            }
        }
        return AddressTemplate.of(resolved);
    }
}
