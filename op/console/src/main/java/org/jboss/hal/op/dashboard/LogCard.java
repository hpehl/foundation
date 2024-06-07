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
package org.jboss.hal.op.dashboard;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.patternfly.component.card.CardBody;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.alert;
import static java.util.stream.Collectors.joining;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.pre;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LINES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_LOG_FILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TAIL;
import static org.jboss.hal.op.skeleton.Domain.domainModeNotSupported;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardActions.cardActions;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardHeader.cardHeader;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.icon.IconSets.fas.exclamationCircle;
import static org.patternfly.icon.IconSets.fas.redo;
import static org.patternfly.style.Size.xs;

class LogCard implements DashboardCard {

    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final HTMLElement root;
    private final CardBody cardBody;

    LogCard(Environment environment, StatementContext statementContext, Dispatcher dispatcher) {
        this.environment = environment;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.root = card()
                .addHeader(cardHeader()
                        .addTitle(cardTitle().textContent("Log"))
                        .addActions(cardActions()
                                .add(button().plain().icon(redo()).onClick((e, c) -> refresh()))))
                .addBody(cardBody = cardBody().style("max-height: 20em;overflow-y: auto"))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void refresh() {
        removeChildrenFrom(cardBody);
        if (environment.standalone()) {
            ResourceAddress address = AddressTemplate.of("subsystem=logging/log-file=server.log").resolve(statementContext);
            Operation operation = new Operation.Builder(address, READ_LOG_FILE)
                    .param(LINES, 50)
                    .param(TAIL, true)
                    .build();
            dispatcher.execute(operation,
                    result -> {
                        String lines = result.asList()
                                .stream()
                                .map(ModelNode::asString)
                                .collect(joining("\n"));
                        cardBody.add(pre().textContent(lines));
                    },
                    (op, error) -> cardBody.add(emptyState().size(xs)
                            .addHeader(emptyStateHeader()
                                    .icon(exclamationCircle())
                                    .text("Log file not found"))
                            .addBody(emptyStateBody()
                                    .add("The log file ")
                                    .add(code().textContent("server.log"))
                                    .add(" was not found!"))
                            .addFooter(emptyStateFooter()
                                    .addActions(emptyStateActions()
                                            .add(button("Choose log file")
                                                    .link()
                                                    .onClick((event, component) -> chooseLogFile()))))));
        } else {
            // TODO Add support for domain mode
            cardBody.add(domainModeNotSupported(xs));
        }
    }

    private void chooseLogFile() {
        // TODO Implement choose log file
        alert("Not yet implemented");
    }
}
