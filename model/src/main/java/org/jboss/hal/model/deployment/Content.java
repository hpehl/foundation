/*
 *  Copyright 2022 Red Hat
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
package org.jboss.hal.model.deployment;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ARCHIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPLODED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HASH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNTIME_NAME;

/** A deployment in the content repository. Can be exploded or archived, managed or unmanaged. */
public class Content extends NamedNode {

    // ------------------------------------------------------ instance

    private final List<ServerGroupDeployment> serverGroupDeployments;

    public Content(ModelNode node) {
        super(node);
        this.serverGroupDeployments = new ArrayList<>();

        boolean managed = true;
        boolean archived = true;
        if (node.hasDefined(CONTENT) && !node.get(CONTENT).asList().isEmpty()) {
            ModelNode content = node.get(CONTENT).asList().get(0);
            if (content.hasDefined(HASH)) {
                managed = true;
            } else if (content.hasDefined(PATH)) {
                managed = false;
            }
            if (content.hasDefined(ARCHIVE)) {
                archived = content.get(ARCHIVE).asBoolean(false);
            }
        }
        // simplify access and set the flags directly into the model node
        get(EXPLODED).set(!archived);
        get(MANAGED).set(managed);
    }

    @Override
    public String toString() {
        return "Content(" + name() + ", " +
                (exploded() ? "exploded" : "archived") + ", " +
                (managed() ? "managed" : "unmanaged") + ", deployed to " +
                serverGroupDeployments + ")";
    }

    // ------------------------------------------------------ api

    public boolean exploded() {
        return get(EXPLODED).asBoolean(false);
    }

    public boolean managed() {
        return get(MANAGED).asBoolean(true);
    }

    public boolean enabled() {
        return hasDefined(ENABLED) && get(ENABLED).asBoolean(false);
    }

    public String runtimeName() {
        ModelNode runtimeName = get(RUNTIME_NAME);
        return runtimeName.isDefined() ? runtimeName.asString() : null;
    }

    public List<ServerGroupDeployment> serverGroupDeployments() {
        return serverGroupDeployments;
    }

    public boolean isDeployedTo(String serverGroup) {
        return serverGroupDeployments.stream()
                .anyMatch(sgd -> serverGroup.equals(sgd.serverGroup()));
    }

    public void addDeployment(ServerGroupDeployment serverGroupDeployment) {
        serverGroupDeployments.add(serverGroupDeployment);
    }
}
