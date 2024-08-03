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

import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Settings;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.StatementContextResolver;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.logger.Level.DEBUG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_CONTROL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.COMBINED_DESCRIPTIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOCALE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE_DEPTH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRIM_DESCRIPTIONS;
import static org.jboss.hal.meta.processing.RepositoryStatus.ALL_PRESENT;
import static org.jboss.hal.meta.processing.RepositoryStatus.NOTHING_PRESENT;
import static org.jboss.hal.meta.processing.RepositoryStatus.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.RrdParser.parseComposite;

/** Creates, executes and parses the {@code read-resource-description} operations to read metadata. */
class RrdTask implements Task<ProcessingContext> {

    private static final Logger logger = Logger.getLogger(MetadataProcessor.class.getName());
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Settings settings;
    private final int batchSize;
    private final int depth;

    RrdTask(Dispatcher dispatcher, StatementContext statementContext, Settings settings, int batchSize, int depth) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.settings = settings;
        this.batchSize = batchSize;
        this.depth = depth;
    }

    @Override
    public Promise<ProcessingContext> apply(ProcessingContext context) {
        List<Task<ProcessingContext>> tasks = new ArrayList<>();

        // create and partition operations
        List<Operation> operations = createRrd(context);
        List<List<Operation>> piles = partition(operations, batchSize);
        List<Composite> composites = piles.stream().map(Composite::new).collect(toList());
        for (Composite composite : composites) {
            tasks.add((ProcessingContext pc) -> dispatcher.execute(composite).then(result -> {
                parseComposite(composite, result, context.rrdResult);
                return Promise.resolve(pc);
            }));
        }

        if (!tasks.isEmpty()) {
            if (logger.isEnabled(DEBUG)) {
                String ops = composites.stream().map(Composite::asCli).collect(joining(", "));
                logger.debug("About to execute %d composite operations: %s", composites.size(), ops);
            }
            return Flow.sequential(context, tasks).promise();
        } else {
            logger.debug("No DMR operations necessary");
            return Promise.resolve(context);
        }
    }

    private List<Operation> createRrd(ProcessingContext context) {
        RepositoryStatus repositoryStatus = context.repositoryStatus;
        List<Operation> operations = new ArrayList<>();
        for (AddressTemplate template : repositoryStatus.templates()) {
            int missingMetadata = repositoryStatus.missingMetadata(template);
            if (missingMetadata != ALL_PRESENT) {

                ResourceAddress address = template.resolve(new StatementContextResolver(statementContext));
                Operation.Builder builder = new Operation.Builder(address, READ_RESOURCE_DESCRIPTION_OPERATION)
                        .param(OPERATIONS, true);

                if (missingMetadata == NOTHING_PRESENT) {
                    builder.param(ACCESS_CONTROL, COMBINED_DESCRIPTIONS);
                } else if (missingMetadata == RESOURCE_DESCRIPTION_PRESENT) {
                    builder.param(ACCESS_CONTROL, TRIM_DESCRIPTIONS);
                }

                if (context.recursive) {
                    builder.param(RECURSIVE_DEPTH, depth);
                }

                String locale = settings.get(Settings.Key.LOCALE).value();
                builder.param(LOCALE, locale);
                operations.add(builder.build());
            }
        }
        return operations;
    }

    private List<List<Operation>> partition(List<Operation> operations, int size) {
        List<List<Operation>> piles = new ArrayList<>();
        for (int i = 0; i < operations.size(); i += size) {
            piles.add(new ArrayList<>(operations.subList(i, Math.min(i + size, operations.size()))));
        }
        return piles;
    }
}
