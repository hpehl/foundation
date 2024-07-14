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
package org.jboss.hal.meta.description;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;

import static java.util.Collections.emptyList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALTERNATIVES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPRECATED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRES;

public class Attribute extends NamedNode {

    private final Attributes attributes;

    public Attribute(Attributes attributes, Property property) {
        super(property);
        this.attributes = attributes;
    }

    public Attribute(Attributes attributes, String name, ModelNode node) {
        super(name, node);
        this.attributes = attributes;
    }

    /**
     * @return the alternatives of this attribute or an empty list if this attribute has no alternatives
     */
    public List<Attribute> alternatives() {
        return find(ALTERNATIVES);
    }

    /**
     * Returns the attributes which require the specified attribute.
     *
     * @param path the path to look for the attribute
     * @param name the name of the attribute which is required by the matching attributes
     * @return the attributes which require {@code} or an empty list if no attributes require {@code name} or if there's no
     * attribute {@code name}
     */
    public List<Attribute> requires(String path, String name) {
        return find(REQUIRES);
    }

    public boolean deprecated() {
        return hasDefined(DEPRECATED) && get(DEPRECATED).asBoolean();
    }

    public Deprecation deprecation() {
        if (deprecated()) {
            return new Deprecation(get(DEPRECATED));
        }
        return null;
    }

    private List<Attribute> find(String name) {
        if (hasDefined(name)) {
            List<Attribute> found = new ArrayList<>();
            get(name).asList().stream()
                    .map(ModelNode::asString)
                    .forEach(attribute -> found.add(attributes.get(attribute)));
            return found;
        }
        return emptyList();
    }
}
