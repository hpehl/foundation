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
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.list.DataListCell;
import org.patternfly.layout.flex.Flex;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.small;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserEngine.parseChildren;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.SINGLETON_FOLDER;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.component.list.DataList.dataList;
import static org.patternfly.component.list.DataListAction.dataListAction;
import static org.patternfly.component.list.DataListCell.dataListCell;
import static org.patternfly.component.list.DataListItem.dataListItem;
import static org.patternfly.icon.IconSets.fas.ban;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Direction.column;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.Gap.md;
import static org.patternfly.style.Size.sm;

class ResourcesElement implements IsElement<HTMLElement> {

    private final UIContext uic;
    private final ModelBrowserTree tree;
    private final ModelBrowserNode parent;
    private final Metadata metadata;
    private final HTMLElement root;

    ResourcesElement(UIContext uic, ModelBrowserTree tree, ModelBrowserNode parent, Metadata metadata) {
        this.uic = uic;
        this.tree = tree;
        this.parent = parent;
        this.metadata = metadata;
        this.root = div().element();

        Operation operation = new Operation.Builder(parent.template.parent().resolve(), READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, parent.name)
                .build();
        uic.dispatcher().execute(operation, result -> {
            List<ModelBrowserNode> children = parseChildren(parent, result, false);
            if (children.isEmpty()) {
                empty();
            } else {
                children(children);
            }
        });
    }

    private void empty() {
        root.append(emptyState().size(sm)
                .addHeader(emptyStateHeader()
                        .icon(ban())
                        .text("No child resources"))
                .addBody(emptyStateBody()
                        .textContent("This resource has no child resources."))
                .element());
    }

    private void children(List<ModelBrowserNode> children) {
        root.appendChild(dataList()
                .addItems(children, child -> {
                    String childId = Id.build(child.name);
                    Metadata childMetadata = parent.type == SINGLETON_FOLDER
                            ? uic.metadataRepository().get(child.template)
                            : metadata;
                    return dataListItem(childId)
                            .addCell(nameCell(childId, child, childMetadata))
                            .addAction(dataListAction()
                                    .add(button("View")
                                            .secondary()
                                            .onClick((e, c) -> tree.select(parent, child))));
                })
                .element());
    }

    private DataListCell nameCell(String childId, ModelBrowserNode child, Metadata metadata) {
        Flex flex = flex().direction(column);
        if (parent.type == SINGLETON_FOLDER) {
            Stability stability = metadata.resourceDescription.stability();
            if (uic.environment().highlightStability(stability)) {
                flex.add(flex().alignItems(center).columnGap(md)
                        .add(flexItem().id(childId).textContent(child.name))
                        .add(flexItem().add(stabilityLabel(stability))));
            } else {
                flex.addItem(flexItem().id(childId).textContent(child.name));
            }
            flex.add(small().textContent(metadata.resourceDescription.description()));
        } else {
            flex.addItem(flexItem().id(childId).textContent(child.name));
        }
        return dataListCell().add(flex);
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
