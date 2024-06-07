package org.jboss.hal.model.deployment;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.model.server.Server;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE_DEPTH;

@ApplicationScoped
public class Deployments {

    private final Dispatcher dispatcher;

    @Inject
    public Deployments(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Promise<List<Deployment>> readStandaloneDeployments() {
        Operation operation = new Operation.Builder(ResourceAddress.root(),
                READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, DEPLOYMENT)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE_DEPTH, 2)
                .build();
        return dispatcher.execute(operation)
                .then(result -> Promise.resolve(result.asPropertyList().stream()
                        .map(property -> new Deployment(Server.standalone(), property.getValue()))
                        .collect(toList())));
    }
}
