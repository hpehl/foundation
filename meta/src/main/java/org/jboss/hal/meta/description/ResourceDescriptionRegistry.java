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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Cache;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.TemplateResolver;

@ApplicationScoped
public class ResourceDescriptionRegistry {

    private static final int CAPACITY = 500;
    private static final Logger logger = Logger.getLogger(ResourceDescriptionRegistry.class.getName());

    private final TemplateResolver resolver;
    private final Cache<String, ResourceDescription> cache;
    // TODO PouchDB 2nd level cache

    @Inject
    public ResourceDescriptionRegistry(StatementContext statementContext) {
        resolver = new ResourceDescriptionResolver(statementContext);
        cache = new Cache<>(CAPACITY, (resourceAddress, __) -> {
            logger.debug("Remove %s from cache", resourceAddress);
            // TODO Add to PouchDB 2nd level cache, if not already there
        });
    }

    // ------------------------------------------------------ api

    public boolean contains(AddressTemplate template) {
        String address = resolveTemplate(template);
        return internalContains(address);
    }

    public ResourceDescription get(AddressTemplate template) {
        String address = resolveTemplate(template);
        ResourceDescription resourceDescription = cache.get(address);
        if (resourceDescription != null) {
            logger.debug("Get resource description for %s as %s from cache", template, address);
            return resourceDescription;
        } else {
            logger.warn("No resource description found for %s", template);
            return null;
        }
    }

    public void add(ResourceAddress resourceAddress, ResourceDescription description) {
        // don't update existing resource descriptions!
        String address = resolveTemplate(AddressTemplate.of(resourceAddress.toString()));
        if (!internalContains(address)) {
            logger.debug("Add resource description for %s as %s", resourceAddress, address);
            internalAdd(address, description);
        }
    }

    // ------------------------------------------------------ internal

    private String resolveTemplate(AddressTemplate template) {
        return resolver.resolve(template).template;
    }

    private boolean internalContains(String address) {
        return cache.contains(address);
        // TODO Check PouchDB 2nd level cache and add it to the cache if needed
    }

    private void internalAdd(String address, ResourceDescription description) {
        cache.put(address, description);
    }
}
