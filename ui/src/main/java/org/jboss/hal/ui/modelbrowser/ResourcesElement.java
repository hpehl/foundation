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
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.Severity;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.small;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.FOLDER;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.SINGLETON_FOLDER;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.list.DataList.dataList;
import static org.patternfly.component.list.DataListAction.dataListAction;
import static org.patternfly.component.list.DataListCell.dataListCell;
import static org.patternfly.component.list.DataListItem.dataListItem;
import static org.patternfly.layout.flex.Direction.column;
import static org.patternfly.layout.flex.Flex.flex;

class ResourcesElement implements IsElement<HTMLElement> {

    private final UIContext uic;
    private final ModelBrowserTree tree;
    private final HTMLElement root;

    ResourcesElement(UIContext uic, ModelBrowserTree tree) {
        this.uic = uic;
        this.tree = tree;
        this.root = div().element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    void show(ModelBrowserNode parent, List<ModelBrowserNode> children) {
        clear();
        if (children.isEmpty()) {
            root.append(alert(Severity.info, "No child resources")
                    .inline()
                    .addDescription("This resource contains no child resources.")
                    .element());
        } else {
            root.appendChild(dataList()
                    .addItems(children, node -> {
                        String childId = Id.build(node.name);
                        AddressTemplate template = parent.type == FOLDER ? parent.template : node.template;
                        Metadata metadata = uic.metadataLookup.get(template);
                        return dataListItem(childId)
                                .addCell(dataListCell()
                                        .add(flex().direction(column)
                                                .add(p().id(childId).textContent(node.name))
                                                .run(flex -> {
                                                    if (parent.type == SINGLETON_FOLDER) {
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
    }

    private void clear() {
        removeChildrenFrom(root);
    }
}
