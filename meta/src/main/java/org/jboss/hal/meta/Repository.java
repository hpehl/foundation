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

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_RECURSIVE;

public abstract class Repository<T extends ModelNode> {

    private static final Logger logger = Logger.getLogger(Repository.class.getName());
    private final String type;
    private final TemplateResolver resolver;
    private final Cache<String, T> cache;
    // TODO PouchDB 2nd level cache


    protected Repository(int capacity, String type, TemplateResolver resolver) {
        this.type = type;
        this.resolver = resolver;
        this.cache = new Cache<>(capacity, (resourceAddress, __) -> {
            logger.debug("Remove %s %s from cache", type, resourceAddress);
            // TODO Add to PouchDB 2nd level cache, if not already there
        });
    }

    // ------------------------------------------------------ api

    public boolean contains(AddressTemplate template) {
        String address = resolveTemplate(template);
        return internalContains(address);
    }

    public T get(AddressTemplate template) {
        String address = resolveTemplate(template);
        T entry = cache.get(address);
        if (entry != null) {
            logger.debug("Get %s for %s as %s from cache", type, template, address);
            return entry;
        } else {
            logger.warn("No %s found for %s", type, template);
            return null;
        }
    }

    public boolean add(ResourceAddress resourceAddress, T entry, boolean recursive) {
        String address = resolveTemplate(AddressTemplate.of(resourceAddress.toString()));
        if (!internalContains(address) || updateExisting()) {
            logger.debug("Add %s for %s as %s (%s)", type, resourceAddress, address, recursive ? "recursive" : "non-recursive");
            entry.get(HAL_RECURSIVE).set(recursive);
            internalAdd(address, entry);
            return true;
        }
        return false;
    }

    // ------------------------------------------------------ internal

    protected boolean updateExisting() {
        return false;
    }

    private String resolveTemplate(AddressTemplate template) {
        return resolver.resolve(template).template;
    }

    private boolean internalContains(String address) {
        return cache.contains(address);
        // TODO Check PouchDB 2nd level cache and add it to the cache if needed
    }

    private void internalAdd(String address, T description) {
        cache.put(address, description);
    }
}
