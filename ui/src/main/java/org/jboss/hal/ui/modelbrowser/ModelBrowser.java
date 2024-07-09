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
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.component.tree.TreeView;
import org.patternfly.layout.grid.GridItem;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.ui.modelbrowser.Node.readNodes;
import static org.patternfly.component.tree.TreeView.treeView;
import static org.patternfly.component.tree.TreeViewType.selectableItems;
import static org.patternfly.layout.grid.Grid.grid;
import static org.patternfly.layout.grid.GridItem.gridItem;

public class ModelBrowser implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static ModelBrowser modelBrowser(Dispatcher dispatcher, AddressTemplate address) {
        return new ModelBrowser(dispatcher, address);
    }

    // ------------------------------------------------------ instance

    static final String NODE = "node";
    private final TreeView treeView;
    private final GridItem detail;
    private final HTMLElement rootElement;

    public ModelBrowser(Dispatcher dispatcher, AddressTemplate address) {
        this.rootElement = grid()
                .addItem(gridItem().span(4)
                        .add(treeView = treeView(selectableItems).guides()))
                .addItem(detail = gridItem().span(8))
                .element();

        Operation operation = new Operation.Builder(address.resolve(), READ_CHILDREN_TYPES_OPERATION)
                .param(INCLUDE_SINGLETONS, true)
                .build();
        dispatcher.execute(operation, result -> {
            List<Node> nodes = readNodes(address, result);
            treeView.addItems(nodes, new NodeFunction(dispatcher));
        });

    }

    @Override
    public HTMLElement element() {
        return rootElement;
    }
}
