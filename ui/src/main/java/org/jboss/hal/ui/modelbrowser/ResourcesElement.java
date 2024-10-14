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

import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.Keys;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.list.DataList;
import org.patternfly.component.list.DataListCell;
import org.patternfly.component.list.DataListItem;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;
import org.patternfly.layout.flex.Flex;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.isAttached;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.small;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserEngine.parseChildren;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.FOLDER;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.SINGLETON_FOLDER;
import static org.jboss.hal.ui.modelbrowser.ResourcesToolbar.resourcesToolbar;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.component.list.DataList.dataList;
import static org.patternfly.component.list.DataListAction.dataListAction;
import static org.patternfly.component.list.DataListCell.dataListCell;
import static org.patternfly.component.list.DataListItem.dataListItem;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.icon.IconSets.fas.ban;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Direction.column;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.Gap.md;
import static org.patternfly.style.Size.sm;

class ResourcesElement implements IsElement<HTMLElement> {

    private static final Logger logger = Logger.getLogger(AttributesTable.class.getName());

    private final UIContext uic;
    private final ModelBrowserTree tree;
    private final ModelBrowserNode parent;
    private final Metadata metadata;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final Filter<ModelBrowserNode> filter;
    private final NoMatch<ModelBrowserNode> noMatch;
    private final Operation operation;
    private final ResourcesToolbar toolbar;
    private final HTMLElement resourcesElement;
    private final HTMLElement root;
    private String missingChild;
    private DataList dataList;

    ResourcesElement(UIContext uic, ModelBrowserTree tree, ModelBrowserNode parent, Metadata metadata) {
        this.uic = uic;
        this.tree = tree;
        this.parent = parent;
        this.metadata = metadata;
        this.visible = ov(0);
        this.total = ov(0);
        this.filter = new ResourcesFilter().onChange(this::onFilterChanged);
        this.noMatch = new NoMatch<>(filter);
        this.operation = new Operation.Builder(parent.template.parent().resolve(), READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, parent.name)
                .build();
        this.root = div()
                .add(toolbar = resourcesToolbar(this, filter, visible, total))
                .add(resourcesElement = div().element())
                .element();

        load();
    }

    private Promise<List<ModelBrowserNode>> load() {
        setVisible(toolbar, false);
        removeChildrenFrom(resourcesElement);
        if (dataList != null) {
            dataList.clear();
        }
        return uic.dispatcher().execute(operation).then(result -> {
            List<ModelBrowserNode> allChildren = parseChildren(parent, result, true);
            List<ModelBrowserNode> existingChildren = allChildren.stream().filter(child -> child.exists).collect(toList());
            List<ModelBrowserNode> missingChildren = allChildren.stream().filter(child -> !child.exists).collect(toList());
            missingChild = missingChildren.size() == 1 ? missingChildren.get(0).name : null;
            if (existingChildren.isEmpty()) {
                empty();
                return Promise.resolve(emptyList());
            } else {
                visible.set(existingChildren.size());
                total.set(existingChildren.size());
                toolbar.toggleAddButton(supportsAdd(parent, allChildren));
                children(existingChildren);
                return Promise.resolve(existingChildren);
            }
        });
    }

    private boolean supportsAdd(ModelBrowserNode parent, List<ModelBrowserNode> children) {
        if (parent.type == ModelBrowserNode.Type.FOLDER) {
            return true;
        } else if (parent.type == ModelBrowserNode.Type.SINGLETON_FOLDER) {
            for (ModelBrowserNode child : children) {
                if (!child.exists) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ status

    private void empty() {
        setVisible(toolbar, false);
        removeChildrenFrom(resourcesElement);
        resourcesElement.appendChild(emptyState().size(sm)
                .addHeader(emptyStateHeader()
                        .icon(ban())
                        .text("No child resources"))
                .addBody(emptyStateBody()
                        .textContent("This resource has no child resources."))
                .addFooter(emptyStateFooter()
                        .addActions(emptyStateActions()
                                .add(button("Add").link().onClick((e, b) -> add()))
                                .add(button("Refresh").link().onClick((e, b) -> refresh()))))
                .element());
    }

    private void children(List<ModelBrowserNode> children) {
        setVisible(toolbar, true);
        if (dataList == null) {
            dataList = dataList();
        }
        dataList.addItems(children, child -> {
            String childId = Id.build(child.name);
            Metadata childMetadata = parent.type == SINGLETON_FOLDER
                    ? uic.metadataRepository().get(child.template)
                    : metadata;
            return dataListItem(childId)
                    .store(Keys.MODEL_BROWSER_NODE, child)
                    .addCell(nameCell(childId, child, childMetadata))
                    .addAction(dataListAction()
                            .run(dataListAction -> {
                                if (parent.type == FOLDER) {
                                    // There are no individual descriptions, so center the button horizontally
                                    dataListAction.style("align-items", "center");
                                }
                            })
                            .add(button("View")
                                    .tertiary()
                                    .onClick((e, c) -> tree.select(parent, child)))
                            .run(dataListAction -> {
                                if (childMetadata.resourceDescription().operations().supports(REMOVE)) {
                                    dataListAction.add(button("Remove")
                                            .tertiary()
                                            .onClick((e, c) -> remove(child)));
                                }
                            }));
        });
        if (!isAttached(dataList)) {
            resourcesElement.appendChild(dataList.element());
        }
    }

    private DataListCell nameCell(String childId, ModelBrowserNode child, Metadata metadata) {
        Flex flex = flex().direction(column);
        if (parent.type == SINGLETON_FOLDER) {
            Stability stability = metadata.resourceDescription().stability();
            if (uic.environment().highlightStability(stability)) {
                flex.add(flex().alignItems(center).columnGap(md)
                        .add(flexItem().id(childId).textContent(child.name))
                        .add(flexItem().add(stabilityLabel(stability))));
            } else {
                flex.addItem(flexItem().id(childId).textContent(child.name));
            }
            flex.add(small().textContent(metadata.resourceDescription().description()));
        } else {
            flex.addItem(flexItem().id(childId).textContent(child.name));
        }
        return dataListCell()
                .run(cell -> {
                    if (parent.type == FOLDER) {
                        cell.style("align-self", "center");
                    }
                })
                .add(flex);
    }

    // ------------------------------------------------------ filter

    private void onFilterChanged(Filter<ModelBrowserNode> filter, String origin) {
        if (dataList != null) {
            logger.debug("Filter resources: %s", filter);
            int matchingItems;
            if (filter.defined()) {
                matchingItems = 0;
                for (DataListItem item : dataList.items()) {
                    ModelBrowserNode mbn = item.get(Keys.MODEL_BROWSER_NODE);
                    if (mbn != null) {
                        boolean match = filter.match(mbn);
                        item.classList().toggle(halModifier(HalClasses.filtered), !match);
                        if (match) {
                            matchingItems++;
                        }
                    }
                }
                noMatch.toggle(resourcesElement, matchingItems == 0);
            } else {
                matchingItems = total.get();
                noMatch.toggle(resourcesElement, false);
                dataList.items().forEach(dlg -> dlg.classList().remove(halModifier(HalClasses.filtered)));
            }
            visible.set(matchingItems);
        }
    }

    // ------------------------------------------------------ action handlers

    void add() {
        ModelBrowserEvents.AddResource.dispatch(element(), parent.template, missingChild, parent.type == SINGLETON_FOLDER);
    }

    void refresh() {
        load().then(children -> {
            if (filter.defined()) {
                onFilterChanged(filter, null);
            }
            return null;
        });
    }

    private void remove(ModelBrowserNode child) {

    }
}
