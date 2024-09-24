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
import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.tree.TreeView;
import org.patternfly.component.tree.TreeViewItem;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.modelBrowser;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserEngine.mbn2tvi;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.uniqueId;
import static org.patternfly.component.tree.TreeView.treeView;
import static org.patternfly.component.tree.TreeViewType.selectableItems;
import static org.patternfly.core.AsyncStatus.pending;
import static org.patternfly.core.Roles.tree;

class ModelBrowserTree implements IsElement<HTMLElement> {

    private static final Logger logger = Logger.getLogger(ModelBrowserTree.class.getName());
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

    public boolean contains(String identifier) {
        return treeView.findItem(identifier) != null;
    }

    void show(List<ModelBrowserNode> nodes) {
        treeView.addItems(nodes, mbn2tvi(uic.dispatcher()));
    }

    void select(String identifier) {
        treeView.select(identifier);
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

    void select(AddressTemplate template) {
        if (!template.isEmpty() && template.fullyQualified()) {
            TreeViewItem item = treeView.findItem(uniqueId(template));
            if (item != null) {
                treeView.select(item);
            } else {
                Flow.sequential(new FlowContext(), selectTasks(template)).subscribe(context -> {
                    if (context.successful()) {
                        // The template might contain invalid segments. Build a template up to the last valid segment.
                        AddressTemplate current = AddressTemplate.root();
                        for (Segment segment : template) {
                            if (treeView.findItem(uniqueId(current.append(segment.key, segment.value))) == null) {
                                break;
                            }
                            current = current.append(segment.key, segment.value);
                        }
                        treeView.select(uniqueId(current));
                    } else {
                        logger.error("Unable to select template %s: %s", template, context.failure());
                    }
                });
            }
        } else {
            logger.error("Unable to select %s: Invalid template for selection", template);
        }
    }

    // ------------------------------------------------------ internal

    private List<Task<FlowContext>> selectTasks(AddressTemplate template) {
        /*
         Relation between the template and the tree view item IDs:

         # Template with 3 segments → 6 item IDs
         subsystem=infinispan/cache-container=web/local-cache=sso
         s-subsystem-e-w
         s-subsystem-e-infinispan
         s-subsystem-e-infinispan-s-cache-container-e-w
         s-subsystem-e-infinispan-s-cache-container-e-web
         s-subsystem-e-infinispan-s-cache-container-e-web-s-local-cache-e-w
         s-subsystem-e-infinispan-s-cache-container-e-web-s-local-cache-e-sso

         # Template with 5 segments → 10 item IDs
         /core-service=management/access=authorization/constraint=application-classification/type=core/classification=deployment
         s-core-service-e-w
         s-core-service-e-management
         s-core-service-e-management-s-access-e-w
         s-core-service-e-management-s-access-e-authorization
         s-core-service-e-management-s-access-e-authorization-s-constraint-e-w
         s-core-service-e-management-s-access-e-authorization-s-constraint-e-application-classification
         s-core-service-e-management-s-access-e-authorization-s-constraint-e-application-classification-s-type-e-w
         s-core-service-e-management-s-access-e-authorization-s-constraint-e-application-classification-s-type-e-core
         s-core-service-e-management-s-access-e-authorization-s-constraint-e-application-classification-s-type-e-core-s-classification-e-w
         s-core-service-e-management-s-access-e-authorization-s-constraint-e-application-classification-s-type-e-core-s-classification-e-deployment
         */
        List<Task<FlowContext>> tasks = new ArrayList<>();
        AddressTemplate current = AddressTemplate.root();
        for (Segment segment : template) {
            String wildcardItemId = uniqueId(current.append(segment.key, "*"));
            String valueItemId = uniqueId(current.append(segment.key, segment.value));
            tasks.add(context -> treeView.load(wildcardItemId).then(items -> context.resolve()));
            tasks.add(context -> treeView.load(valueItemId).then(items -> context.resolve()));
            current = current.append(segment.key, segment.value);
        }
        return tasks;
    }
}
