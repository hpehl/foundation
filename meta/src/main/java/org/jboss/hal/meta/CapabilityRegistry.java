package org.jboss.hal.meta;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.ParallelTasks;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GET_PROVIDER_POINTS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

@ApplicationScoped
public class CapabilityRegistry {

    private static final AddressTemplate TEMPLATE = AddressTemplate.of("{domain.controller}/core-service=capability-registry");
    private static final Logger logger = Logger.getLogger(CapabilityRegistry.class.getName());
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;

    @Inject
    public CapabilityRegistry(Dispatcher dispatcher, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
    }

    public Promise<List<String>> providerPoints(String capability) {
        Operation operation = new Operation.Builder(TEMPLATE.resolve(statementContext), GET_PROVIDER_POINTS)
                .param(NAME, capability)
                .build();
        return dispatcher.execute(operation)
                .then(result -> {
                    List<String> providerPoints = result.asList().stream().map(ModelNode::asString).collect(toList());
                    logger.debug("Provider points for %s: %s", capability, providerPoints);
                    return Promise.resolve(providerPoints);
                });
    }

    public Promise<AddressTemplate> findReference(String capability, String value) {
        List<Task<FlowContext>> tasks = List.of(
                context -> providerPoints(capability).then(context::resolve),
                context -> {
                    List<String> providerPoints = context.pop();
                    List<Task<FlowContext>> nestedTasks = providerPoints.stream()
                            .map(pp -> new ReadProviderPoint(dispatcher, capability, value, pp))
                            .collect(toList());
                    return new ParallelTasks<>(nestedTasks, false).apply(context);
                });
        return Flow.sequential(new FlowContext(), tasks)
                .failFast(false)
                .then(context -> Promise.resolve(context.<AddressTemplate>pop(null)))
                .catch_(error -> {
                    logger.error("Unable to find capability %s for %s", capability, value);
                    return Promise.resolve(((AddressTemplate) null));
                });
    }

    private static class ReadProviderPoint implements Task<FlowContext> {

        private final Dispatcher dispatcher;
        private final String providerPoint;
        private final String capability;
        private final String value;

        private ReadProviderPoint(Dispatcher dispatcher, String capability, String value, String providerPoint) {
            this.dispatcher = dispatcher;
            this.capability = capability;
            this.value = value;
            this.providerPoint = providerPoint;
        }

        @Override
        public Promise<FlowContext> apply(FlowContext context) {
            logger.debug("Read provider point %s for %s and %s", providerPoint, capability, value);
            ResourceAddress address = AddressTemplate.of(providerPoint).resolve();
            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(ATTRIBUTES_ONLY, true)
                    .build();
            return dispatcher.execute(operation, false)
                    .then(result -> {
                        if (result.isDefined()) {
                            if (result.getType() == ModelType.LIST) {
                                Optional<AddressTemplate> any = result.asList().stream()
                                        .filter(node -> node.hasDefined(ADDRESS))
                                        .map(node -> AddressTemplate.of(new ResourceAddress(node.get(ADDRESS))))
                                        .filter(template -> value.equals(template.last().value))
                                        .findAny();
                                if (any.isPresent()) {
                                    return found(context, any.get());
                                }
                            } else if (result.getType() == ModelType.OBJECT) {
                                return found(context, AddressTemplate.of(address));
                            }
                        }
                        return context.resolve(); // not found
                    })
                    .catch_(error -> {
                        logger.debug("%s is not a valid resource for %s and %s", address, capability, value);
                        return context.resolve();  // ignore errors
                    });
        }

        private Promise<FlowContext> found(FlowContext context, AddressTemplate template) {
            logger.debug("Found %s for %s and %s", template, capability, value);
            return context.resolve(template);
        }
    }
}
