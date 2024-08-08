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
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.tree.TreeView;
import org.patternfly.component.tree.TreeViewItem;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.modelBrowser;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserEngine.mbn2tvi;
import static org.patternfly.component.tree.TreeView.treeView;
import static org.patternfly.component.tree.TreeViewItemStatus.pending;
import static org.patternfly.component.tree.TreeViewType.selectableItems;
import static org.patternfly.core.Roles.tree;

class ModelBrowserTree implements IsElement<HTMLElement> {

    private final UIContext uic;
    private final TreeView treeView;
    private final HTMLElement root;
    ModelBrowserDetail detail;

    ModelBrowserTree(UIContext uic) {
        this.uic = uic;
        this.treeView = treeView(selectableItems).guides()
                .onSelect((event, treeViewItem, selected) -> {
                    ModelBrowserNode node = treeViewItem.get(ModelBrowserEngine.MODEL_BROWSER_NODE);
                    if (node != null) {
                        detail.show(node);
                    }
                });
        this.root = div().css(halComponent(modelBrowser, tree))
                .add(treeView)
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    void show(List<ModelBrowserNode> nodes) {
        treeView.addItems(nodes, mbn2tvi(uic.dispatcher()));
    }

    void select(ModelBrowserNode parent, ModelBrowserNode mbn) {
        TreeViewItem parentItem = treeView.findItem(parent.id);
        if (parentItem != null && !parentItem.expanded() && parentItem.status() == pending) {
            parentItem.load().then(__ -> {
                treeView.select(mbn.id);
                return null;
            });
        } else {
            treeView.select(mbn.id);
        }
    }
}
