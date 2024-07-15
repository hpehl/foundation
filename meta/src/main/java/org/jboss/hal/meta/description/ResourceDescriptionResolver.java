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
import java.util.List;

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.StatementContextResolver;
import org.jboss.hal.meta.TemplateResolver;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_CONFIG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.meta.Placeholder.SELECTED_RESOURCE;

/**
 * Template resolver for the {@link ResourceDescriptionRegistry}.
 */
class ResourceDescriptionResolver implements TemplateResolver {

    private final StatementContext statementContext;
    private final StatementContextResolver statementContextResolver;

    ResourceDescriptionResolver(StatementContext statementContext) {
        this.statementContext = statementContext;
        this.statementContextResolver = new StatementContextResolver(statementContext);
    }

    @Override
    public AddressTemplate resolve(AddressTemplate template) {
        if (!template.isEmpty()) {
            int index = 0;
            int length = template.size();
            List<Segment> segments = new ArrayList<>();
            AddressTemplate resolved = statementContextResolver.resolve(template);

            for (Segment segment : resolved) {
                String key = segment.key;
                String value = segment.value;
                if (segment.containsPlaceholder() && SELECTED_RESOURCE.equals(segment.placeholder())) {
                    value = "*";
                } else if (!statementContext.standalone()) {
                    switch (key) {
                        case HOST:
                            if (length > 1 && index == 0) {
                                value = "*";
                            }
                            break;

                        case PROFILE:
                        case SERVER_GROUP:
                            if (index == 0) {
                                value = "*";
                            }
                            break;

                        case SERVER:
                        case SERVER_CONFIG:
                            if (index == 1) {
                                value = "*";
                            }
                            break;

                        default:
                            break;
                    }
                }
                segments.add(new Segment(key, value));
                index++;
            }
            return AddressTemplate.of(segments);
        }
        return template;
    }
}
