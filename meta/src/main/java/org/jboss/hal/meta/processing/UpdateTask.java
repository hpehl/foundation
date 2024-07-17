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

import java.util.Map;

import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionRepository;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextRepository;

import elemental2.promise.Promise;

class UpdateTask implements Task<ProcessingContext> {

    private static final Logger logger = Logger.getLogger(UpdateTask.class.getName());
    private final ResourceDescriptionRepository resourceDescriptionRepository;
    private final SecurityContextRepository securityContextRepository;

    UpdateTask(ResourceDescriptionRepository resourceDescriptionRepository,
            SecurityContextRepository securityContextRepository) {
        this.resourceDescriptionRepository = resourceDescriptionRepository;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    public Promise<ProcessingContext> apply(ProcessingContext context) {
        if (context.rrdResult.shouldUpdate()) {
            int[] counter = new int[2];
            for (Map.Entry<ResourceAddress, ResourceDescription> entry : context.rrdResult.resourceDescriptions.entrySet()) {
                ResourceAddress address = entry.getKey();
                ResourceDescription resourceDescription = entry.getValue();
                if (resourceDescriptionRepository.add(address, resourceDescription, context.recursive)) {
                    counter[0]++;
                }
            }
            for (Map.Entry<ResourceAddress, SecurityContext> entry : context.rrdResult.securityContexts.entrySet()) {
                ResourceAddress address = entry.getKey();
                SecurityContext securityContext = entry.getValue();
                if (securityContextRepository.add(address, securityContext, context.recursive)) {
                    counter[1]++;
                }
            }
            logger.debug("Added %d resource descriptions and %d security contexts to the repositories",
                    counter[0], counter[1]);
        }
        return Promise.resolve(context);
    }
}
