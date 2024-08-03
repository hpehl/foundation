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
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionRepository;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextRepository;

import elemental2.promise.Promise;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_RECURSIVE;
import static org.jboss.hal.meta.processing.RepositoryStatus.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.RepositoryStatus.SECURITY_CONTEXT_PRESENT;

/** Task which checks whether metadata is already present in the repositories. */
class CheckTask implements Task<ProcessingContext> {

    private static final Logger logger = Logger.getLogger(CheckTask.class.getName());
    private final ResourceDescriptionRepository resourceDescriptionRepository;
    private final SecurityContextRepository securityContextRepository;

    CheckTask(ResourceDescriptionRepository resourceDescriptionRepository,
            SecurityContextRepository securityContextRepository) {
        this.resourceDescriptionRepository = resourceDescriptionRepository;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    public Promise<ProcessingContext> apply(final ProcessingContext context) {
        check(context.repositoryStatus, context.recursive);
        logger.debug("%s", context.repositoryStatus);
        return Promise.resolve(context);
    }

    boolean allPresent(Set<AddressTemplate> templates, boolean recursive) {
        RepositoryStatus repositoryStatus = new RepositoryStatus(templates);
        check(repositoryStatus, recursive);
        return repositoryStatus.allPresent();
    }

    private void check(RepositoryStatus lookupResult, boolean recursive) {
        for (AddressTemplate template : lookupResult.templates()) {
            if (resourceDescriptionRepository.contains(template)) {
                if (!recursive) {
                    lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
                } else {
                    ResourceDescription resourceDescription = resourceDescriptionRepository.get(template);
                    if (resourceDescription.get(HAL_RECURSIVE).asBoolean(false)) {
                        lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
                    }
                }
            }
            if (securityContextRepository.contains(template)) {
                if (!recursive) {
                    lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
                } else {
                    SecurityContext securityContext = securityContextRepository.get(template);
                    if (securityContext.get(HAL_RECURSIVE).asBoolean(false)) {
                        lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
                    }
                }
            }
        }
    }
}
