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

import java.util.List;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_CONTROL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILDREN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXCEPTIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MODEL_DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STEPS;

public class RrdParser {

    private static final Logger logger = Logger.getLogger(RrdParser.class.getName());

    // ------------------------------------------------------ composite

    static void parseComposite(Composite composite, CompositeResult compositeResult, RrdResult rrdResult) {
        int index = 0;
        for (ModelNode step : compositeResult) {
            if (step.isFailure()) {
                throw new ParserException("Failed step 'step-" + (index + 1) + "' in composite rrd result: " + step
                        .getFailureDescription());
            }

            ModelNode stepResult = step.get(RESULT);
            if (stepResult.getType() == ModelType.LIST) {
                // multiple rrd results each with its own address
                for (ModelNode modelNode : stepResult.asList()) {
                    ModelNode result = modelNode.get(RESULT);
                    if (result.isDefined()) {
                        ResourceAddress operationAddress = operationAddress(composite, index);
                        ResourceAddress resultAddress = new ResourceAddress(modelNode.get(ADDRESS));
                        ResourceAddress resolvedAddress = makeFqAddress(operationAddress, resultAddress);
                        parseSingle(resolvedAddress, result, rrdResult);
                    }
                }

            } else {
                // a single rrd result
                ResourceAddress address = operationAddress(composite, index);
                parseSingle(address, stepResult, rrdResult);
            }
            index++;
        }
    }

    private static ResourceAddress operationAddress(Composite composite, int index) {
        List<ModelNode> steps = composite.get(STEPS).asList();
        if (index >= steps.size()) {
            throw new ParserException("Cannot get operation at index " + index + " from composite " + composite);
        }
        ModelNode operation = steps.get(index);
        return new ResourceAddress(operation.get(ADDRESS));
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static ResourceAddress makeFqAddress(ResourceAddress operationAddress, ResourceAddress resultAddress) {
        ResourceAddress resolved = resultAddress;
        List<Property> operationSegments = operationAddress.asPropertyList();
        List<Property> resultSegments = resultAddress.asPropertyList();

        // For rrd operations against running servers using wildcards like /host=primary/server=server-one/interface=*
        // the result does *not* contain absolute addresses. Since we need them in the registries,
        // this method fixes this corner case.
        if (operationSegments.size() > 2 &&
                operationSegments.size() == resultSegments.size() + 2 &&
                HOST.equals(operationSegments.get(0).getName()) &&
                SERVER.equals(operationSegments.get(1).getName())) {
            resolved = new ResourceAddress()
                    .add(HOST, operationSegments.get(0).getValue().asString())
                    .add(SERVER, operationSegments.get(1).getValue().asString())
                    .add(resultAddress);
            logger.debug("Adjust result address '{}' -> '{}'", resultAddress, resolved);
        }
        return resolved;
    }

    // ------------------------------------------------------ single

    private static void parseSingle(ResourceAddress address, ModelNode modelNode, RrdResult rrdResult) {
        if (modelNode.getType() == ModelType.LIST) {
            for (ModelNode nestedNode : modelNode.asList()) {
                ResourceAddress nestedAddress = new ResourceAddress(nestedNode.get(ADDRESS));
                ModelNode nestedResult = nestedNode.get(RESULT);
                parseSingleNode(nestedAddress, nestedResult, rrdResult);
            }
        } else {
            parseSingleNode(address, modelNode, rrdResult);
        }
    }

    private static void parseSingleNode(ResourceAddress address, ModelNode modelNode, RrdResult rrdResult) {
        // resource description
        // to reduce the payload, we only use the flat model node w/o children
        ModelNode childrenNode = modelNode.hasDefined(CHILDREN) ? modelNode.remove(CHILDREN) : new ModelNode();
        if (!rrdResult.containsResourceDescription(address) && modelNode.hasDefined(DESCRIPTION)) {
            rrdResult.addResourceDescription(address, new ResourceDescription(modelNode));
        }

        // security context
        ModelNode accessControl = modelNode.get(ACCESS_CONTROL);
        if (accessControl.isDefined()) {
            if (!rrdResult.containsSecurityContext(address) && accessControl.hasDefined(DEFAULT)) {
                rrdResult.addSecurityContext(address, new SecurityContext(accessControl.get(DEFAULT)));
            }

            // exceptions
            if (accessControl.hasDefined(EXCEPTIONS)) {
                List<Property> exceptions = accessControl.get(EXCEPTIONS).asPropertyList();
                for (Property property : exceptions) {
                    ModelNode exception = property.getValue();
                    ResourceAddress exceptionAddress = new ResourceAddress(exception.get(ADDRESS));
                    if (!rrdResult.containsSecurityContext(exceptionAddress)) {
                        rrdResult.addSecurityContext(exceptionAddress, new SecurityContext(exception));
                    }
                }
            }
        }

        // children
        if (childrenNode.isDefined()) {
            List<Property> children = childrenNode.asPropertyList();
            for (Property child : children) {
                String addressKey = child.getName();
                if (child.getValue().hasDefined(MODEL_DESCRIPTION)) {
                    List<Property> modelDescriptions = child.getValue().get(MODEL_DESCRIPTION).asPropertyList();
                    for (Property modelDescription : modelDescriptions) {
                        String addressValue = modelDescription.getName();
                        ModelNode childNode = modelDescription.getValue();
                        ResourceAddress childAddress = new ResourceAddress(address).add(addressKey, addressValue);
                        parseSingleNode(childAddress, childNode, rrdResult);
                    }
                }
            }
        }
    }
}
