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

import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.UIContext;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.small;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserEngine.parseChildren;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.FOLDER;
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
import static org.patternfly.layout.flex.Direction.column;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.style.Size.xs;

class ResourcesElement implements IsElement<HTMLElement> {

    private final HTMLElement root;

    ResourcesElement(UIContext uic, ModelBrowserTree tree, ModelBrowserNode parent) {
        this.root = div().element();

        List<Task<FlowContext>> tasks = new ArrayList<>();
        Operation operation = new Operation.Builder(parent.template.parent().resolve(), READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, parent.name)
                .build();
        tasks.add(context -> uic.dispatcher.execute(operation).then(result -> {
            List<ModelBrowserNode> children = parseChildren(parent, result, false);
            return context.resolve(children);
        }));
        if (parent.type == SINGLETON_FOLDER) {
            tasks.add(context -> uic.metadataRepository.lookup(parent.template).then(__ -> context.resolve()));
        }
        Flow.parallel(new FlowContext(), tasks).subscribe(context -> {
            if (context.successful()) {
                List<ModelBrowserNode> children = context.pop();
                if (children.isEmpty()) {
                    empty();
                } else {
                    children(uic, tree, parent, children);
                }
            } else {
                // TODO Error handling
            }
        });
    }

    private void empty() {
        root.append(emptyState().size(xs)
                .addHeader(emptyStateHeader()
                        .icon(ban())
                        .text("No child resources"))
                .addBody(emptyStateBody()
                        .textContent("This resource has no child resources."))
                .element());
    }

    private void children(UIContext uic, ModelBrowserTree tree, ModelBrowserNode parent, List<ModelBrowserNode> children) {
        root.appendChild(dataList()
                .addItems(children, node -> {
                    String childId = Id.build(node.name);
                    AddressTemplate template = parent.type == FOLDER ? parent.template : node.template;
                    return dataListItem(childId)
                            .addCell(dataListCell()
                                    .add(flex().direction(column)
                                            .add(p().id(childId).textContent(node.name))
                                            .run(flex -> {
                                                if (parent.type == SINGLETON_FOLDER) {
                                                    Metadata metadata = uic.metadataRepository.get(template);
                                                    flex.add(small().textContent(
                                                            metadata.resourceDescription.description()));
                                                }
                                            })))
                            .addAction(dataListAction()
                                    .add(button("View")
                                            .secondary()
                                            .onClick((e, c) -> tree.select(parent, node))));
                })
                .element());
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
