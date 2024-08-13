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

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.meta.description.OperationDescriptions;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.list.List;
import org.patternfly.component.table.Table;
import org.patternfly.layout.flex.Flex;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescription;
import static org.jboss.hal.ui.BuildingBlocks.attributeName;
import static org.jboss.hal.ui.BuildingBlocks.operationDescription;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.patternfly.component.label.Label.label;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.layout.flex.SpaceItems.xs;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Color.blue;
import static org.patternfly.style.Width.width20;
import static org.patternfly.style.Width.width35;
import static org.patternfly.style.Width.width45;

// TODO Implement toolbar with filters/flags:
//  Find an operation
//  Filter global/non-global operations
//  Filter by return-value: void/non-void
class OperationsTable implements IsElement<HTMLElement> {

    private static final String OPERATION = "modelbrowser.operation";
    private final UIContext uic;
    private final Table table;

    OperationsTable(UIContext uic, ResourceDescription resource, OperationDescriptions operations) {
        this.uic = uic;
        this.table = table()
                .addHead(thead()
                        .addRow(tr("operations-head")
                                .addItem(th("name").width(width35).textContent("Name"))
                                .addItem(th("parameters").width(width45).textContent("Parameters"))
                                .addItem(th("return-value").width(width20).textContent("Return value"))))
                .addBody(tbody()
                        .addRows(operations, operation -> {
                            AttributeDescription returnValue = operation.returnValue();
                            return tr(operation.name())
                                    .store(OPERATION, operation)
                                    .addItem(td("Name")
                                            .add(operationName(resource, operation))
                                            .add(operationDescription(operation)))
                                    .addItem(td("Parameters")
                                            .run(td -> {
                                                if (!returnValue.isDefined()) {
                                                    td.colSpan(2);
                                                }
                                            })
                                            .add(parameters(resource, operation)))
                                    .addItem(td("Return value")
                                            .run(td -> {
                                                if (returnValue.isDefined()) {
                                                    td.add(returnValue(returnValue));
                                                }
                                            }));
                        }));
    }

    private Flex operationName(ResourceDescription resource, OperationDescription operation) {
        HTMLContainerBuilder<HTMLElement> name = strong()
                .textContent(operation.name())
                .run(strong -> {
                    if (operation.deprecation().isDefined()) {
                        strong.css(halModifier(deprecated));
                    }
                });
        // I guess it's safe to say that an operation is either global *or* preview or experimental
        if (operation.global()) {
            return flex().spaceItems(sm)
                    .addItem(flexItem().add(name))
                    .add(flexItem().add(label("global", blue)));
        } else if (uic.environment().highlightStability(resource.stability(), operation.stability())) {
            return flex().spaceItems(sm)
                    .addItem(flexItem().add(name))
                    .add(flexItem().add(stabilityLabel(operation.stability())));
        } else {
            return flex().add(name);
        }
    }

    private List parameters(ResourceDescription resource, OperationDescription operation) {
        return list().plain().bordered()
                .addItems(operation.parameters(), parameter -> listItem()
                        .add(attributeName(parameter, true, () -> uic.environment()
                                .highlightStability(resource.stability(), operation.stability(),
                                        parameter.stability()))
                                .alignItems(center).spaceItems(xs)
                                .add(span().textContent(":"))
                                .add(span().textContent(parameter.formatType())))
                        .add(attributeDescription(parameter).css(util("mt-sm"))));
    }

    private HTMLContainerBuilder<HTMLDivElement> returnValue(AttributeDescription returnValue) {
        return div()
                .add(returnValue.formatType())
                .run(div -> {
                    if (returnValue.hasDefined(DESCRIPTION)) {
                        div.add(div().css(util("mt-sm")).add(returnValue.description()));
                    }
                });
    }

    @Override
    public HTMLElement element() {
        return table.element();
    }
}
