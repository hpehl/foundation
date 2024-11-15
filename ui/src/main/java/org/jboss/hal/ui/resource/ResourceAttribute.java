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
import java.util.function.Predicate;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;

import static org.jboss.hal.dmr.ModelType.EXPRESSION;

/** Simple record for an attribute name/value/description triple. */
class ResourceAttribute {

    // ------------------------------------------------------ factories

    static Predicate<AttributeDescription> includes(List<String> attributes) {
        return ad -> {
            if (attributes.isEmpty()) {
                return true;
            }
            return attributes.contains(ad.fullyQualifiedName());
        };
    }

    static Predicate<AttributeDescription> notDeprecated() {
        return ad -> !ad.deprecation().isDefined();
    }

    /**
     * Collects and returns a list of resource attributes based on the provided attribute descriptions.
     *
     * @param metadata     The metadata containing resource descriptions and attribute descriptions.
     * @param descriptions The attribute descriptions.
     * @param predicate    A predicate to filter which attributes should be collected.
     * @return A list of ResourceAttribute objects representing the collected attributes.
     */
    static List<ResourceAttribute> resourceAttributes(Metadata metadata, AttributeDescriptions descriptions,
            Predicate<AttributeDescription> predicate) {
        List<ResourceAttribute> resourceAttributes = new ArrayList<>();
        for (AttributeDescription description : descriptions) {
            if (description.simpleValueType()) {
                AttributeDescriptions nestedDescriptions = description.valueTypeAttributeDescriptions();
                for (AttributeDescription nestedDescription : nestedDescriptions) {
                    if (predicate.test(nestedDescription)) {
                        resourceAttributes.add(new ResourceAttribute(new ModelNode(), metadata, nestedDescription));
                    }
                }
            } else {
                if (predicate.test(description)) {
                    resourceAttributes.add(new ResourceAttribute(new ModelNode(), metadata, description));
                }
            }
        }
        return resourceAttributes;
    }

    /**
     * Collects and returns a list of resource attributes based on an existing resource.
     *
     * @param resource  The model node representing the resource.
     * @param metadata  The metadata containing resource descriptions and attribute descriptions.
     * @param predicate A predicate to filter which attributes should be collected.
     * @return A list of ResourceAttribute objects representing the collected attributes.
     */
    static List<ResourceAttribute> resourceAttributes(ModelNode resource, Metadata metadata,
            Predicate<AttributeDescription> predicate) {
        List<ResourceAttribute> resourceAttributes = new ArrayList<>();
        for (Property property : resource.asPropertyList()) {
            String name = property.getName();
            AttributeDescription description = metadata.resourceDescription().attributes().get(name);
            if (description.simpleValueType()) {
                AttributeDescriptions nestedDescriptions = description.valueTypeAttributeDescriptions();
                for (AttributeDescription nestedDescription : nestedDescriptions) {
                    if (predicate.test(nestedDescription)) {
                        ModelNode nestedValue = ModelNodeHelper.nested(resource, nestedDescription.fullyQualifiedName());
                        resourceAttributes.add(new ResourceAttribute(nestedValue, metadata, nestedDescription));
                    }
                }
            } else {
                if (predicate.test(description)) {
                    ModelNode value = property.getValue();
                    resourceAttributes.add(new ResourceAttribute(value, metadata, description));
                }
            }
        }
        return resourceAttributes;
    }

    // ------------------------------------------------------ instance

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
}
