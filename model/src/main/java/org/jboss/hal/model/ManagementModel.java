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
package org.jboss.hal.model;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.model.ManagementModel.TraverseType.WILDCARD_RESOURCES;

@ApplicationScoped
public class ManagementModel {

    private static Set<String> STANDARD_EXCLUDES = Set.of(
            "/core-service=capability-registry",
            "/core-service=management/access",
            "/core-service=management/service",
            "/core-service=platform-mbean",
            "/core-service=server-environment",
            "/core-service=service-container"
    );

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;

    @Inject
    public ManagementModel(Dispatcher dispatcher, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
    }

    // ------------------------------------------------------ traverse

    public enum TraverseType {
        NON_EXISTING_SINGLETONS,
        WILDCARD_RESOURCES,
    }

    public static class TraverseContext {

        private boolean running;
        private int resources;

        public TraverseContext() {
            running = true;
            resources = 0;
        }

        private void progress(int resources) {
            this.resources += resources;
        }

        public void stop() {
            running = false;
        }

        public int resources() {
            return resources;
        }
    }

    public Promise<TraverseContext> traverse(AddressTemplate template, Set<String> exclude, Set<TraverseType> traverseType,
            Consumer<ResourceAddress> addressConsumer) {
        return read(new TraverseContext(), template, exclude, traverseType, addressConsumer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Promise<TraverseContext> read(TraverseContext context, AddressTemplate template, Set<String> exclude,
            Set<TraverseType> traverseType, Consumer<ResourceAddress> addressConsumer) {
        return readChildren(template, traverseType)
                .then(children -> {
                    context.progress(children.size());
                    Promise[] promises = children.stream()
                            .peek(child -> {
                                if (traverseType.contains(WILDCARD_RESOURCES) || child.fullyQualified()) {
                                    addressConsumer.accept(child.resolve(statementContext));
                                }
                            })
                            .map(child -> read(context, child, exclude, traverseType, addressConsumer))
                            .toArray(Promise[]::new);
                    return Promise.all(promises).then(__ -> Promise.resolve(context));
                });
    }

    private Promise<List<AddressTemplate>> readChildren(AddressTemplate template, Set<TraverseType> traverseType) {
        if ("*".equals(template.last().value)) {
            String resource = template.last().key;
            Operation.Builder builder = new Operation.Builder(template.parent().resolve(statementContext),
                    READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, resource);
            if (traverseType.contains(TraverseType.NON_EXISTING_SINGLETONS)) {
                builder.param(INCLUDE_SINGLETONS, true);
            }
            Operation operation = builder.build();
            return dispatcher.execute(operation, false)
                    .then(result -> Promise.resolve(result.asList().stream()
                            .map(modelNode -> template.parent().append(resource, modelNode.asString()))
                            .collect(toList())))
                    .catch_(__ -> Promise.resolve(emptyList()));
        } else {
            Operation operation = new Operation.Builder(template.resolve(statementContext), READ_CHILDREN_TYPES_OPERATION)
                    .param(INCLUDE_SINGLETONS, true)
                    .build();
            return dispatcher.execute(operation, false)
                    .then(result -> Promise.resolve(result.asList().stream()
                            .map(modelNode -> {
                                String name = modelNode.asString();
                                if (name.contains("=")) {
                                    String[] parts = name.split("=", 2);
                                    return template.append(parts[0], parts[1]);
                                } else {
                                    return template.append(name, "*");
                                }
                            }).collect(toList())))
                    .catch_(__ -> Promise.resolve(emptyList()));
        }
    }
}
