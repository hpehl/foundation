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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.ResourceDescriptionRegistry;
import org.jboss.hal.meta.security.SecurityContextRegistry;

import elemental2.promise.Promise;

@ApplicationScoped
public class MetadataProcessor {

    private static final Logger logger = Logger.getLogger(MetadataProcessor.class.getName());
    private final Dispatcher dispatcher;
    private final ResourceDescriptionRegistry resourceDescriptionRegistry;
    private final SecurityContextRegistry securityContextRegistry;

    @Inject
    public MetadataProcessor(Dispatcher dispatcher,
            ResourceDescriptionRegistry resourceDescriptionRegistry,
            SecurityContextRegistry securityContextRegistry) {
        this.dispatcher = dispatcher;
        this.resourceDescriptionRegistry = resourceDescriptionRegistry;
        this.securityContextRegistry = securityContextRegistry;
    }

    public Promise<Void> process(Set<AddressTemplate> templates, boolean recursive) {
        logger.debug("Process metadata for %s (%s)", templates, recursive ? "recursive" : "non-recursive");
        LookupTask lookupRegistries = new LookupTask(resourceDescriptionRegistry, securityContextRegistry);
        if (lookupRegistries.allPresent(templates, recursive)) {
            logger.debug("All metadata have been already processed -> done");
            return Promise.resolve((Void) null);

        } else {
            List<Task<LookupContext>> tasks = new ArrayList<>();
            tasks.add(lookupRegistries);
            return Flow.sequential(new LookupContext(templates, recursive), tasks).then(c -> {
                logger.info("Successfully processed metadata for %s (%s)", templates,
                        recursive ? "recursive" : "non-recursive");
                return Promise.resolve((Void) null);
            });
        }
    }
}
