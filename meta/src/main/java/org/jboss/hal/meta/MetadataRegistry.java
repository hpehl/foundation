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

import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.security.SecurityContextRegistry;

import elemental2.promise.Promise;

/** Registry for {@linkplain Metadata meta-data} */
@ApplicationScoped
public class MetadataRegistry {

    private final ResourceDescriptionRegistry resourceDescriptionRegistry;
    private final SecurityContextRegistry securityContextRegistry;
    private final MetadataProcessor metadataProcessor;

    @Inject
    public MetadataRegistry(ResourceDescriptionRegistry resourceDescriptionRegistry,
            SecurityContextRegistry securityContextRegistry,
            MetadataProcessor metadataProcessor) {
        this.resourceDescriptionRegistry = resourceDescriptionRegistry;
        this.securityContextRegistry = securityContextRegistry;
        this.metadataProcessor = metadataProcessor;
    }

    // ------------------------------------------------------ api

    public Metadata get(AddressTemplate template) throws MissingMetadataException {
        if (!resourceDescriptionRegistry.contains(template) || !securityContextRegistry.contains(template)) {
            throw new MissingMetadataException(template);
        }
        return new Metadata(resourceDescriptionRegistry.get(template), securityContextRegistry.get(template));
    }

    public void lookup(AddressTemplate template, Consumer<Metadata> callback) {
        lookup(template).then(metadata -> {
            callback.accept(metadata);
            return null;
        });
    }

    public Promise<Metadata> lookup(AddressTemplate template) {
        if (resourceDescriptionRegistry.contains(template) && securityContextRegistry.contains(template)) {
            return Promise.resolve(
                    new Metadata(resourceDescriptionRegistry.get(template), securityContextRegistry.get(template)));
        } else {
            return null;
        }
    }
}
