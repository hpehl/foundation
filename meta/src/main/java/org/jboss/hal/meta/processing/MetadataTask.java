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
package org.jboss.hal.meta.processing;

import java.util.Set;

import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionRepository;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextRepository;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.joining;

class MetadataTask implements Task<ProcessingContext> {

    private static final Logger logger = Logger.getLogger(MetadataTask.class.getName());
    private final ResourceDescriptionRepository resourceDescriptionRepository;
    private final SecurityContextRepository securityContextRepository;

    MetadataTask(ResourceDescriptionRepository resourceDescriptionRepository,
            SecurityContextRepository securityContextRepository) {
        this.resourceDescriptionRepository = resourceDescriptionRepository;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    public Promise<ProcessingContext> apply(ProcessingContext context) {
        Metadata metadata = metadata(context.templates);
        if (metadata != null) {
            context.metadata = metadata;
        }
        return Promise.resolve(context);
    }

    Metadata metadata(Set<AddressTemplate> templates) {
        Metadata metadata = null;
        if (templates.size() == 1) {
            AddressTemplate template = templates.iterator().next();
            if (!template.endsWith("*")) {
                ResourceDescription resourceDescription = resourceDescriptionRepository.get(template);
                SecurityContext securityContext = securityContextRepository.get(template);
                metadata = new Metadata(resourceDescription, securityContext);
            }
        }

        if (metadata == null) {
            String values = templates.stream().map(AddressTemplate::toString).collect(joining(", "));
            logger.warn("Metadata lookup for %s resulted in multiple results. Returning an empty metadata.", values);
        }
        return metadata;
    }
}
