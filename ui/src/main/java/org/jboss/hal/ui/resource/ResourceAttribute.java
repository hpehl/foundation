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
package org.jboss.hal.ui.resource;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelType.EXPRESSION;

/** Simple record for an attribute name/value/description triple. */
class ResourceAttribute {

    static List<ResourceAttribute> resourceAttributes(ModelNode resource, Metadata metadata, List<String> attributes) {
        List<ResourceAttribute> resourceAttributes = new ArrayList<>();
        if (attributes.isEmpty()) {
            // collect all properties (including nested, record-like properties)
            for (Property property : resource.asPropertyList()) {
                String name = property.getName();
                ModelNode value = property.getValue();
                AttributeDescription description = metadata.resourceDescription().attributes().get(name);
                if (description.simpleValueType()) {
                    AttributeDescriptions nestedDescriptions = description.valueTypeAttributeDescriptions();
                    for (AttributeDescription nestedDescription : nestedDescriptions) {
                        ModelNode nestedValue = ModelNodeHelper.nested(resource, nestedDescription.fullyQualifiedName());
                        resourceAttributes.add(new ResourceAttribute(nestedValue, metadata, nestedDescription));
                    }
                } else {
                    resourceAttributes.add(new ResourceAttribute(value, metadata, description));
                }
            }
        } else {
            // collect only the specified attributes (which can be nested)
            for (String attribute : attributes) {
                if (attribute.contains(".")) {
                    // TODO Support nested attributes
                } else {
                    ModelNode value = resource.get(attribute);
                    AttributeDescription description = metadata.resourceDescription().attributes().get(attribute);
                    resourceAttributes.add(new ResourceAttribute(value, metadata, description));
                }
            }
        }
        return resourceAttributes;
    }

    final String fqn;
    final String name;
    final ModelNode value;
    final AttributeDescription description;
    final boolean readable;
    final boolean writable;
    final boolean expression;

    ResourceAttribute(ModelNode value, Metadata metadata, AttributeDescription description) {
        this.fqn = description.fullyQualifiedName();
        this.name = description.name();
        this.value = value;
        this.description = description;
        this.expression = value.isDefined() && value.getType() == EXPRESSION;
        if (description.nested()) {
            readable = metadata.securityContext().readable(description.root().name());
            writable = metadata.securityContext().writable(description.root().name());
        } else {
            readable = metadata.securityContext().readable(name);
            writable = metadata.securityContext().writable(name);
        }
    }

    @Override
    public String toString() {
        return fqn + "=" + value.asString();
    }

    boolean booleanValue() {
        boolean booleanValue = false;
        if (value.isDefined()) {
            booleanValue = value.asBoolean(false);
        } else {
            if (description.hasDefined(DEFAULT)) {
                booleanValue = description.get(DEFAULT).asBoolean(false);
            }
        }
        return booleanValue;
    }
}
