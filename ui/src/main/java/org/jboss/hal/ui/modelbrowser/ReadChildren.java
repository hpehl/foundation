package org.jboss.hal.ui.modelbrowser;

import java.util.function.Function;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.patternfly.component.tree.TreeViewItem;

import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.ui.modelbrowser.ModelBrowser.NODE;
import static org.jboss.hal.ui.modelbrowser.Node.readNodes;

class ReadChildren implements Function<TreeViewItem, Promise<Iterable<TreeViewItem>>> {

    private static final Logger logger = Logger.getLogger(ReadChildren.class.getName());
    private final Dispatcher dispatcher;

    ReadChildren(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Promise<Iterable<TreeViewItem>> apply(TreeViewItem treeViewItem) {
        Node node = treeViewItem.get(NODE);
        if (node != null) {
            Operation operation = null;
            if (node.type == NodeType.FOLDER) {
                operation = new Operation.Builder(node.address.parent().resolve(), READ_CHILDREN_NAMES_OPERATION)
                        .param(CHILD_TYPE, node.name)
                        .param(INCLUDE_SINGLETONS, true)
                        .build();
            } else if (node.type == NodeType.SINGLETON_RESOURCE || node.type == NodeType.RESOURCE) {
                operation = new Operation.Builder(node.address.resolve(), READ_CHILDREN_TYPES_OPERATION)
                        .param(INCLUDE_SINGLETONS, true)
                        .build();
            }
            if (operation != null) {
                return dispatcher.execute(operation)
                        .then(result -> Promise.resolve(readNodes(node.address, result).stream()
                                .map(new NodeFunction(dispatcher))
                                .collect(toList())));
            } else {
                logger.error("Unable to read child resources of tree view item %o - %s: Wrong node type %s",
                        treeViewItem.element(), treeViewItem.id, node.type.name());
                return Promise.resolve(emptyList());
            }
        } else {
            logger.error("Unable to read child resources of tree view item %o - %s: No stored node found!",
                    treeViewItem.element(), treeViewItem.id);
            return Promise.resolve(emptyList());
        }
    }
}
