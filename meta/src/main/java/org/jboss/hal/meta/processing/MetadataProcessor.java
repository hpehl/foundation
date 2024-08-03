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
import org.jboss.hal.env.Settings;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.ResourceDescriptionRepository;
import org.jboss.hal.meta.security.SecurityContextRepository;

import elemental2.promise.Promise;

@ApplicationScoped
public class MetadataProcessor {

    /** Recursive depth for the r-r-d operations. Keep this small - some browsers choke on too big payload size */
    static final int RRD_DEPTH = 3;
    /** Number of r-r-d operations part of one composite operation. */
    private static final int BATCH_SIZE = 3;
    private static final Logger logger = Logger.getLogger(MetadataProcessor.class.getName());

    private final Settings settings;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final ResourceDescriptionRepository resourceDescriptionRepository;
    private final SecurityContextRepository securityContextRepository;

    @Inject
    public MetadataProcessor(Settings settings,
            Dispatcher dispatcher,
            StatementContext statementContext,
            ResourceDescriptionRepository resourceDescriptionRepository,
            SecurityContextRepository securityContextRepository) {
        this.settings = settings;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resourceDescriptionRepository = resourceDescriptionRepository;
        this.securityContextRepository = securityContextRepository;
    }

    public Promise<Metadata> process(Set<AddressTemplate> templates, boolean recursive) {
        logger.debug("Process metadata for %s (%s)", templates, recursive ? "recursive" : "non-recursive");
        CheckTask checkTask = new CheckTask(resourceDescriptionRepository, securityContextRepository);
        GetMetadataTask getMetadataTask = new GetMetadataTask(resourceDescriptionRepository, securityContextRepository);

        if (checkTask.allPresent(templates, recursive)) {
            logger.debug("All metadata for %s have been already processed -> done", templates);
            return Promise.resolve(getMetadataTask.metadata(templates));

        } else {
            String handle = logger.timeInfo("Metadata processing");
            List<Task<ProcessingContext>> tasks = new ArrayList<>();
            tasks.add(checkTask);
            tasks.add(new RrdTask(dispatcher, statementContext, settings, BATCH_SIZE, RRD_DEPTH));
            tasks.add(new UpdateTask(resourceDescriptionRepository, securityContextRepository));
            tasks.add(getMetadataTask);
            return Flow.sequential(new ProcessingContext(templates, recursive), tasks)
                    .then(context -> {
                        logger.info("Successfully processed metadata for %s (%s)", templates,
                                recursive ? "recursive" : "non-recursive");
                        return Promise.resolve(context.metadata);
                    })
                    .finally_(() -> logger.timeInfoEnd(handle));
        }
    }
}
