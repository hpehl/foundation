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

import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.IsElement;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.table.Table;
import org.patternfly.component.table.Td;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONFIGURATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.METRIC;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_WRITE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORAGE;
import static org.jboss.hal.ui.form.ModelNodeView.modelNodeView;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.component.tabs.Tab.tab;
import static org.patternfly.component.tabs.TabContent.tabContent;
import static org.patternfly.component.tabs.Tabs.tabs;
import static org.patternfly.component.text.TextContent.textContent;
import static org.patternfly.icon.IconSets.fas.database;
import static org.patternfly.icon.IconSets.fas.edit;
import static org.patternfly.icon.IconSets.fas.lock;
import static org.patternfly.icon.IconSets.fas.memory;
import static org.patternfly.icon.IconSets.patternfly.trendUp;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Width.width10;
import static org.patternfly.style.Width.width20;
import static org.patternfly.style.Width.width60;

class ResourceElement implements IsElement<HTMLElement> {

    private final HTMLElement root;

    ResourceElement(UIContext uic, Metadata metadata, ModelNode modelNode) {
        this.root = tabs()
                .addItem(tab("data", "Data")
                        .addContent(tabContent()
                                .add(modelNodeView(metadata, modelNode).css(util("mt-lg")))))
                .addItem(tab("attributes", "Attributes")
                        .addContent(tabContent()
                                .add(attributes(metadata))))
                .addItem(tab("operations", "Operations")
                        .addContent(tabContent()
                                .add(operations(metadata))))
                .addItem(tab("capabilities", "Capabilities")
                        .addContent(tabContent()
                                .add(capabilities(metadata))))
                .addItem(tab("children", "Children")
                        .addContent(tabContent()
                                .add(children(metadata))))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    private Table attributes(Metadata metadata) {
        return table()
                .addHead(thead().css(util("mt-sm"))
                        .addRow(tr("attributes-head")
                                .addItem(th("name").width(width60).textContent("Name"))
                                .addItem(th("type").width(width20).textContent("Type"))
                                .addItem(th("Storage").width(width10).textContent("Storage"))
                                .addItem(th("Access type").width(width10).textContent("Access type"))))
                .addBody(tbody()
                        .addRows(metadata.resourceDescription.attributes(), attribute -> tr(attribute.name())
                                .addItem(td("Name")
                                        .add(textContent()
                                                .add(p()
                                                        .add(strong().textContent(attribute.name()))
                                                        .add(br())
                                                        .add(attribute.description()))))
                                .addItem(td("Type").textContent(Types.formatType(attribute)))
                                .addItem(td("Storage").run(td -> storage(td, attribute)))
                                .addItem(td("Access type").run(td -> accessType(td, attribute)))
                        ));
    }

    private void storage(Td td, AttributeDescription attribute) {
        if (attribute.hasDefined(STORAGE)) {
            String storage = attribute.get(STORAGE).asString();
            if (CONFIGURATION.equals(storage)) {
                td.add(database());
            } else if (RUNTIME.equals(storage)) {
                td.add(memory());
            } else {
                td.innerHtml(SafeHtmlUtils.fromString("&nbsp;"));
            }
        } else {
            td.innerHtml(SafeHtmlUtils.fromString("&nbsp;"));
        }
    }

    private void accessType(Td td, AttributeDescription attribute) {
        if (attribute.hasDefined(ACCESS_TYPE)) {
            String accessType = attribute.get(ACCESS_TYPE).asString();
            switch (accessType) {
                case READ_WRITE:
                    td.add(edit());
                    break;
                case READ_ONLY:
                    td.add(lock());
                    break;
                case METRIC:
                    td.add(trendUp());
                    break;
                default:
                    td.innerHtml(SafeHtmlUtils.fromString("&nbsp;"));
                    break;
            }
        } else {
            td.innerHtml(SafeHtmlUtils.fromString("&nbsp;"));
        }
    }

    private Table operations(Metadata metadata) {
        return table();
    }

    private Table capabilities(Metadata metadata) {
        return table();
    }

    private Table children(Metadata metadata) {
        return table();
    }
}
