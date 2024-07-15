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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Cache;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.TemplateResolver;

@ApplicationScoped
public class SecurityContextRegistry {

    private static final int CAPACITY = 500;
    private static final Logger logger = Logger.getLogger(SecurityContextRegistry.class.getName());

    private final TemplateResolver resolver;
    private final Cache<String, SecurityContext> cache;
    // TODO PouchDB 2nd level cache

    @Inject
    public SecurityContextRegistry(StatementContext statementContext) {
        resolver = new SecurityContextResolver(statementContext);
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

    public SecurityContext get(AddressTemplate template) {
        String address = resolveTemplate(template);
        SecurityContext securityContext = cache.get(address);
        if (securityContext != null) {
            logger.debug("Get security context for %s as %s from cache", template, address);
            return securityContext;
        } else {
            logger.warn("No security context found for %s", template);
            return null;
        }
    }

    public void add(ResourceAddress resourceAddress, SecurityContext securityContext) {
        // don't update existing resource descriptions!
        String address = resolveTemplate(AddressTemplate.of(resourceAddress.toString()));
        if (!internalContains(address)) {
            logger.debug("Add security context for %s as %s", resourceAddress, address);
            internalAdd(address, securityContext);
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

    private void internalAdd(String address, SecurityContext securityContext) {
        cache.put(address, securityContext);
    }
}
