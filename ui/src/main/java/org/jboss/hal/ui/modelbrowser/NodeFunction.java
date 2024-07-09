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
