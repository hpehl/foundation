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

import java.util.function.Function;

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.patternfly.component.tree.TreeViewItem;

import static org.jboss.hal.ui.modelbrowser.ModelBrowser.NODE;
import static org.patternfly.component.tree.TreeViewItem.treeViewItem;
import static org.patternfly.icon.IconSets.fas.fileAlt;
import static org.patternfly.icon.IconSets.fas.folder;
import static org.patternfly.icon.IconSets.fas.folderOpen;
import static org.patternfly.icon.IconSets.fas.listUl;

/**
 * Function that turns a {@link Node} into a {@link TreeViewItem}.
 */
class NodeFunction implements Function<Node, TreeViewItem> {

    private final Dispatcher dispatcher;

    NodeFunction(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public TreeViewItem apply(Node node) {
        return treeViewItem(id(node))
                .text(node.name)
                .store(NODE, node)
                .run(treeViewItem -> {
                    switch (node.type) {
                        case FOLDER:
                            treeViewItem
                                    .icon(folder())
                                    .expandedIcon(folderOpen())
                                    .addItems(new ReadChildren(dispatcher));
                            break;
                        case SINGLETON_PARENT:
                            treeViewItem
                                    .icon(listUl())
                                    .addItems(node.children,
                                            grandChild -> treeViewItem(id(grandChild))
                                                    .text(grandChild.name)
                                                    .icon(fileAlt())
                                                    .store(NODE, grandChild)
                                                    .addItems(new ReadChildren(dispatcher)));
                            break;
                        case RESOURCE:
                            treeViewItem
                                    .icon(fileAlt())
                                    .addItems(new ReadChildren(dispatcher));
                            break;
                    }
                });
    }

    private String id(Node node) {
        // Using only Id.build(node.address.template)
        // would strip all special characters like /=:*
        // and possibly lead to duplicate ids
        String safeTemplate = node.address.template
                .replace("/", "-s-")
                .replace("=", "-e-")
                .replace(":", "-c-")
                .replace("*", "-w-");
        return Id.build(safeTemplate);
    }
}
