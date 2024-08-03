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

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/** Wrapper around the attributes of a {@link ResourceDescription} */
public class AttributeDescriptions implements Iterable<AttributeDescription> {

    private final LinkedHashMap<String, AttributeDescription> attributes;

    public AttributeDescriptions(ModelNode modelNode) {
        this.attributes = modelNode.asPropertyList()
                .stream()
                .map(property -> new AttributeDescription(this, property))
                .collect(toMap(NamedNode::name, identity(), (existing, replacement) -> replacement, LinkedHashMap::new));
    }

    @Override
    public Iterator<AttributeDescription> iterator() {
        return attributes.values().iterator();
    }

    public AttributeDescription get(String name) {
        return attributes.get(name);
    }
}
