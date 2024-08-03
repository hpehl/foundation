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
package org.jboss.hal.meta;

import java.util.function.Consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptionRepository;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextRepository;

import elemental2.promise.Promise;

import static java.util.Collections.singleton;

/** Class to read {@linkplain Metadata meta-data} */
@ApplicationScoped
public class MetadataLookup {

    private static final Logger logger = Logger.getLogger(MetadataLookup.class.getName());
    private final MetadataRepository metadataRepository;
    private final ResourceDescriptionRepository resourceDescriptionRepository;
    private final SecurityContextRepository securityContextRepository;
    private final MetadataProcessor metadataProcessor;

    @Inject
    public MetadataLookup(MetadataRepository metadataRepository,
            ResourceDescriptionRepository resourceDescriptionRepository,
            SecurityContextRepository securityContextRepository,
            MetadataProcessor metadataProcessor) {
        this.metadataRepository = metadataRepository;
        this.resourceDescriptionRepository = resourceDescriptionRepository;
        this.securityContextRepository = securityContextRepository;
        this.metadataProcessor = metadataProcessor;
    }

    // ------------------------------------------------------ api

    /**
     * Retrieves metadata for the given address template.
     *
     * @param template the address template to retrieve metadata for
     * @return the metadata associated with the address template, or empty metadata if not found
     */
    public Metadata get(AddressTemplate template) {
        if (!metadataRepository.contains(template)) {
            logger.error("Missing metadata for %s" + template.toString());
            return Metadata.empty();
        }
        return metadataRepository.get(template);
    }

    /**
     * Performs a lookup for metadata based on the given address template.
     *
     * @param template the address template to perform the lookup for
     * @param callback the consumer to accept the retrieved metadata
     */
    public void lookup(AddressTemplate template, Consumer<Metadata> callback) {
        lookup(template).then(metadata -> {
            callback.accept(metadata);
            return null;
        });
    }

    /**
     * Performs a lookup for metadata based on the given address template.
     *
     * @param template the address template to perform the lookup for
     * @return a Promise representing the lookup result, containing the metadata associated with the address template
     */
    public Promise<Metadata> lookup(AddressTemplate template) {
        if (resourceDescriptionRepository.contains(template) && securityContextRepository.contains(template)) {
            ResourceDescription resourceDescription = resourceDescriptionRepository.get(template);
            SecurityContext securityContext = securityContextRepository.get(template);
            return Promise.resolve(new Metadata(resourceDescription, securityContext));
        } else {
            return metadataProcessor.process(singleton(template), false);
        }
    }
}
