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

import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.tree.TreeView;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.ui.modelbrowser.DetailPane.detailPane;
import static org.jboss.hal.ui.modelbrowser.Node.readNodes;
import static org.patternfly.component.tree.TreeView.treeView;
import static org.patternfly.component.tree.TreeViewType.selectableItems;
import static org.patternfly.layout.grid.Grid.grid;
import static org.patternfly.layout.grid.GridItem.gridItem;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Variable.componentVar;

public class ModelBrowser implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static ModelBrowser modelBrowser(UIContext uic, AddressTemplate template) {
        return new ModelBrowser(uic, template);
    }

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(ModelBrowser.class.getName());
    static final String NODE = "node";
    private final TreeView treeView;
    private final DetailPane detailPane;
    private final HTMLElement root;

    public ModelBrowser(UIContext uic, AddressTemplate address) {
        root = grid().span(12).gutter()
                .addItem(gridItem().span(4)
                        .css("hal-model-browser-tree")
                        .add(treeView = treeView(selectableItems).guides()))
                .addItem(gridItem().span(8)
                        .add(detailPane = detailPane(uic)))
                .element();
        componentVar(component(Classes.treeView), "PaddingTop").applyTo(treeView).set(0);
        treeView.onSelect((event, treeViewItem, selected) -> {
            Node node = treeViewItem.get(NODE);
            if (node != null) {
                detailPane.show(node);
            }
        });

        if (address.endsWith("*")) {
            logger.error("Illegal address: %s. Please specify a fully qualified address not ending with '*'", address);
        } else {
            Operation operation = new Operation.Builder(address.resolve(), READ_CHILDREN_TYPES_OPERATION)
                    .param(INCLUDE_SINGLETONS, true)
                    .build();
            uic.dispatcher.execute(operation, result -> {
                List<Node> nodes = readNodes(address, operation.getName(), result);
                treeView.addItems(nodes, new NodeFunction(uic.dispatcher));
                detailPane.show(new Node(address, null, NodeType.RESOURCE));
            });
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
