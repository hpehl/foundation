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
package org.jboss.hal.ui.modelbrowser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.ui.modelbrowser.NodeType.FOLDER;
import static org.jboss.hal.ui.modelbrowser.NodeType.RESOURCE;
import static org.jboss.hal.ui.modelbrowser.NodeType.SINGLETON_PARENT;
import static org.jboss.hal.ui.modelbrowser.NodeType.SINGLETON_RESOURCE;

/**
 * Holds data necessary to create tree view items from the management model. The node class is the layer between the model nodes
 * and the tree view items.
 */
class Node {

    /**
     * Read nodes from a DMR payload.
     *
     * @param address the address template of the DMR operation
     * @param result  the operation result
     * @return a list of Node objects created from the operation result
     */
    static List<Node> readNodes(AddressTemplate address, String operation, ModelNode result) {
        Map<String, Node> nodes = new LinkedHashMap<>();
        for (ModelNode modelNode : result.asList()) {
            String name = modelNode.asString();
            if (operation.equals(READ_CHILDREN_TYPES_OPERATION)) {
                int index = name.indexOf("=");
                if (index != -1 && !name.equals("=")) {
                    String singleton = name.substring(0, index);
                    String child = name.substring(index + 1);
                    Node node = nodes.computeIfAbsent(singleton,
                            key -> new Node(address.append(key, "*"), key, SINGLETON_PARENT));
                    node.children.add(new Node(address.append(name), child, SINGLETON_RESOURCE));
                } else {
                    if (address.template.endsWith("*")) {
                        nodes.put(name, new Node(address.parent().append(address.last().key, name), name, RESOURCE));
                    } else {
                        nodes.put(name, new Node(address.append(name, "*"), name, FOLDER));
                    }
                }
            } else {
                if (address.template.endsWith("*")) {
                    nodes.put(name, new Node(address.parent().append(address.last().key, name), name, RESOURCE));
                } else {
                    nodes.put(name, new Node(address.append(name, "*"), name, FOLDER));
                }
            }
        }
        return new ArrayList<>(nodes.values());
    }

    final AddressTemplate address;
    final String name;
    final NodeType type;
    final List<Node> children;

    Node(AddressTemplate address, String name, NodeType type) {
        this.address = address;
        this.name = name;
        this.type = type;
        this.children = new ArrayList<>();
    }
}
