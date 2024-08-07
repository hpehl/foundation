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

import org.jboss.elemento.IsElement;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.table.Table;
import org.patternfly.component.tabs.Tabs;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.ui.form.ModelNodeView.modelNodeView;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.tabs.Tab.tab;
import static org.patternfly.component.tabs.TabContent.tabContent;
import static org.patternfly.style.Classes.util;

class ResourceElement implements IsElement<HTMLElement> {

    private final HTMLElement root;

    ResourceElement(UIContext uic, ModelBrowserNode mbn, Metadata metadata) {
        this.root = div().element();
        Operation operation = new Operation.Builder(mbn.template.resolve(uic.statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        uic.dispatcher.execute(operation, resource -> details(resource, metadata));
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    private void details(ModelNode resource, Metadata metadata) {
        root.append(Tabs.tabs()
                .addItem(tab("data", "Data")
                        .addContent(tabContent()
                                .add(modelNodeView(metadata, resource).css(util("mt-lg")))))
                .run(tabs -> {
                    if (!metadata.resourceDescription.attributes().isEmpty()) {
                        tabs.addItem(tab("attributes", "Attributes")
                                .addContent(tabContent()
                                        .add(new AttributesTable(metadata.resourceDescription.attributes()))));
                    }
                })
                .run(tabs -> {
                    if (!metadata.resourceDescription.operations().isEmpty()) {
                        tabs.addItem(tab("operations", "Operations")
                                .addContent(tabContent()
                                        .add(new OperationsTable(metadata.resourceDescription.operations()))));
                    }
                })
                .addItem(tab("capabilities", "Capabilities")
                        .addContent(tabContent()
                                .add(capabilities(metadata))))
                .addItem(tab("children", "Children")
                        .addContent(tabContent()
                                .add(children(metadata))))
                .element());
    }

    private Table capabilities(Metadata metadata) {
        return table();
    }

    private Table children(Metadata metadata) {
        return table();
    }
}
