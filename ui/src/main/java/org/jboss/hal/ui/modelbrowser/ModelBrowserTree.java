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
import java.util.EnumSet;
import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.model.ManagementModel.TraverseType;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.button.Button;
import org.patternfly.component.tooltip.Tooltip;
import org.patternfly.component.tree.TreeView;
import org.patternfly.component.tree.TreeViewItem;
import org.patternfly.style.Sticky;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.console;
import static java.util.Collections.emptySet;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserEngine.MODEL_BROWSER_NODE;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserEngine.mbn2tvi;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.uniqueId;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.page.PageMainSection.pageMainSection;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarGroupType.iconButtonGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.component.tree.TreeView.treeView;
import static org.patternfly.component.tree.TreeViewType.selectableItems;
import static org.patternfly.core.AsyncStatus.pending;
import static org.patternfly.core.Roles.tree;
import static org.patternfly.icon.IconSets.far.minusSquare;
import static org.patternfly.icon.IconSets.fas.arrowLeft;
import static org.patternfly.icon.IconSets.fas.arrowRight;
import static org.patternfly.icon.IconSets.fas.home;
import static org.patternfly.icon.IconSets.fas.sync;
import static org.patternfly.popper.Placement.bottom;
import static org.patternfly.popper.Placement.bottomStart;
import static org.patternfly.style.Classes.insetNone;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Padding.noPadding;

class ModelBrowserTree implements IsElement<HTMLElement> {

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(ModelBrowserTree.class.getName());
    private final UIContext uic;
    private final ModelBrowser modelBrowser;
    private final History<TreeViewItem> history;
    private final Button backButton;
    private final Button forwardButton;
    private final TreeView treeView;
    private final HTMLElement root;
    private Tooltip backTooltip;
    private Tooltip forwardTooltip;

    ModelBrowserTree(UIContext uic, ModelBrowser modelBrowser) {
        this.uic = uic;
        this.modelBrowser = modelBrowser;
        this.history = new History<>();

        treeView = treeView(selectableItems).guides()
                .onSelect((event, treeViewItem, selected) -> navigate(treeViewItem, true));
        backButton = button().plain().icon(arrowLeft()).disabled().onClick((event, component) -> back());
        forwardButton = button().plain().icon(arrowRight()).disabled().onClick((event, component) -> forward());
        Button reloadButton = button().plain().icon(sync()).onClick((e, b) -> reload());
        Button homeButton = button().plain().icon(home()).onClick((e, b) -> modelBrowser.home());
        GotoResource gotoResource = new GotoResource(this);
        Button collapseButton = button().plain().icon(minusSquare()).onClick((e, b) -> treeView.collapse());

        tooltip(reloadButton.element(), "Refresh").placement(bottom).appendToBody();
        tooltip(homeButton.element(), "Home").placement(bottom).appendToBody();
        tooltip(collapseButton.element(), "Collapse all").placement(bottom).appendToBody();
        tooltip(collapseButton.element(), "Collapse all").placement(bottom).appendToBody();
        tooltip(gotoResource.element(), "Go to resource").placement(bottom).appendToBody();

        root = div().css(halComponent(HalClasses.modelBrowser, tree))
                .add(pageMainSection().sticky(Sticky.top).padding(noPadding)
                        .add(toolbar().css(modifier(insetNone))
                                .addContent(toolbarContent()
                                        .addGroup(toolbarGroup(iconButtonGroup)
                                                .addItem(toolbarItem().add(backButton))
                                                .addItem(toolbarItem().add(forwardButton))
                                                .addItem(toolbarItem().add(reloadButton))
                                                .addItem(toolbarItem().add(homeButton))
                                                .addItem(toolbarItem().add(gotoResource))
                                                .addItem(toolbarItem().add(collapseButton))))))
                .add(pageMainSection().padding(noPadding).add(treeView))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ load

    void load(List<ModelBrowserNode> nodes) {
        treeView.clear();
        treeView.addItems(nodes, mbn2tvi(uic.dispatcher()));
    }

    private void reload() {
        if (!treeView.selectedItems().isEmpty()) {
            treeView.selectedItems().get(0).reload();

            TreeViewItem treeViewItem = treeView.selectedItems().get(0);
            ModelBrowserNode mbn = treeViewItem.get(MODEL_BROWSER_NODE);
            if (mbn != null) {
                traverse(mbn.template);
            }
        } else {
            // no selection → reload root
            modelBrowser.reload();
            traverse(AddressTemplate.root());
        }
    }

    private void traverse(AddressTemplate template) {
        uic.managementModel().traverse(template, emptySet(), EnumSet.noneOf(TraverseType.class),
                        resourceAddress -> console.log("### %s", resourceAddress))
                .then(context -> {
                    console.log("=== Done: %d!", context.resources());
                    return null;
                });
    }

    // ------------------------------------------------------ select

    void select(String identifier) {
        treeView.select(identifier);
    }

    void select(ModelBrowserNode parent, ModelBrowserNode child) {
        TreeViewItem parentItem = treeView.findItem(parent.id);
        if (parentItem != null) {
            if (!parentItem.expanded() && parentItem.status() == pending) {
                parentItem.load().then(__ -> {
                    treeView.select(child.id);
                    return null;
                });
            } else if (!parentItem.contains(child.id)) {
                // child might have been added externally in CLI or other management tools
                parentItem.reload().then(__ -> {
                    treeView.select(child.id);
                    return null;
                });
            } else {
                treeView.select(child.id);
            }
        } else {
            treeView.select(child.id);
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

    void unselect() {
        treeView.unselect(false);
    }

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

    // ------------------------------------------------------ navigation

    private void back() {
        if (history.canGoBack()) {
            TreeViewItem treeViewItem = history.back();
            treeView.select(treeViewItem, true, false);
            navigate(treeViewItem, false);
        }
    }

    private void forward() {
        if (history.canGoForward()) {
            TreeViewItem treeViewItem = history.forward();
            treeView.select(treeViewItem, true, false);
            navigate(treeViewItem, false);
        }
    }

    private void navigate(TreeViewItem treeViewItem, boolean updateHistory) {
        if (updateHistory) {
            history.navigate(treeViewItem);
        }
        updateNavigationButtons();
        ModelBrowserNode node = treeViewItem.get(ModelBrowserEngine.MODEL_BROWSER_NODE);
        if (node != null) {
            modelBrowser.detail.show(node);
        }
    }

    private void updateNavigationButtons() {
        backButton.disabled(!history.canGoBack());
        forwardButton.disabled(!history.canGoForward());

        if (history.canGoBack()) {
            ModelBrowserNode node = null;
            TreeViewItem treeViewItem = history.peekBack();
            if (treeViewItem != null) {
                node = treeViewItem.get(ModelBrowserEngine.MODEL_BROWSER_NODE);
            }
            if (backTooltip == null) {
                backTooltip = tooltip(backButton.element(), "")
                        .placement(bottomStart)
                        .appendToBody();
            }
            if (node != null) {
                backTooltip.text("Go back to " + node.template);
            }
        } else {
            failSafeRemoveFromParent(backTooltip);
            backTooltip = null;
        }

        if (history.canGoForward()) {
            ModelBrowserNode node = null;
            TreeViewItem treeViewItem = history.peekForward();
            if (treeViewItem != null) {
                node = treeViewItem.get(ModelBrowserEngine.MODEL_BROWSER_NODE);
            }
            if (forwardTooltip == null) {
                forwardTooltip = tooltip(forwardButton.element(), "")
                        .placement(bottomStart)
                        .appendToBody();
            }
            if (node != null) {
                forwardTooltip.text("Go forward to " + node.template);
            }
        } else {
            failSafeRemoveFromParent(forwardTooltip);
            forwardTooltip = null;
        }
    }
}
