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

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Placeholder;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.StatementContextResolver;
import org.jboss.hal.meta.TemplateResolver;

import static org.jboss.hal.meta.Placeholder.DOMAIN_CONTROLLER;
import static org.jboss.hal.meta.Placeholder.SELECTED_HOST;
import static org.jboss.hal.meta.Placeholder.SELECTED_SERVER;
import static org.jboss.hal.meta.Placeholder.SELECTED_SERVER_GROUP;

/**
 * Template resolver for the {@link SecurityContextRepository}.
 * <p>
 * This resolver
 * <ol>
 *     <li>
 *         replaces the values of the segments with "*" depending on the operation mode and
 *         whether the segment contains a certain placeholder:
 *         <table>
 *             <thead><tr><td>Segment</td><td>Replacement</td><td>Condition</td></tr></thead>
 *             <tbody>
 *                 <tr><td>{domain.controller}</td><td>no replacement</td><td>domain mode</td></tr>
 *                 <tr><td>{selected.host}</td><td>no replacement</td><td>domain mode</td></tr>
 *                 <tr><td>{selected.profile}</td><td>profile=*</td><td>domain mode</td></tr>
 *                 <tr><td>{selected.server-group}</td><td>no replacement</td><td>domain mode</td></tr>
 *                 <tr><td>{selected.server-config}</td><td>server-config=*</td><td>domain mode</td></tr>
 *                 <tr><td>{selected.server}</td><td>no replacement</td><td>domain mode</td></tr>
 *                 <tr><td>{selected.deployment}</td><td>deployment=*</td><td>always</td></tr>
 *                 <tr><td>foo={selected.resource}</td><td>foo=*</td><td>always</td></tr>
 *                 <tr><td>&lt;anything else&gt;</td><td>no replacement</td><td>always</td></tr>
 *             </tbody>
 *         </table>
 *     </li>
 *     <li>
 *         resolves the template against the {@link StatementContext}
 *     </li>
 * </ol>
 */
public class SecurityContextResolver implements TemplateResolver {

    private static final Set<Placeholder> PRESERVE = new HashSet<>();

    static {
        PRESERVE.add(DOMAIN_CONTROLLER);
        PRESERVE.add(SELECTED_HOST);
        PRESERVE.add(SELECTED_SERVER);
        PRESERVE.add(SELECTED_SERVER_GROUP);
    }

    private final StatementContext statementContext;
    private final StatementContextResolver statementContextResolver;

    SecurityContextResolver(StatementContext statementContext) {
        this.statementContext = statementContext;
        this.statementContextResolver = new StatementContextResolver(statementContext);
    }

    @Override
    public AddressTemplate resolve(AddressTemplate template) {
        List<Segment> segments = new ArrayList<>();
        for (Segment segment : template) {
            if (segment.containsPlaceholder()) {
                Placeholder placeholder = segment.placeholder();
                if (segment.hasKey()) {
                    segments.add(new Segment(segment.key, "*"));
                } else {
                    boolean matchingOperationMode = !placeholder.domainOnly || !statementContext.standalone();
                    if (matchingOperationMode && !PRESERVE.contains(placeholder)) {
                        segments.add(new Segment(segment.key, "*"));
                    } else {
                        segments.add(segment);
                    }
                }
            } else {
                segments.add(segment);
            }
        }
        AddressTemplate resolved = AddressTemplate.of(segments);
        return statementContextResolver.resolve(resolved);
    }
}
