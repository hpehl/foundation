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

import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardActions.cardActions;
import static org.patternfly.component.card.CardHeader.cardHeader;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.icon.IconSets.fas.redo;

class RuntimeCard implements DashboardCard {

    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final HTMLElement root;

    RuntimeCard(StatementContext statementContext, Dispatcher dispatcher) {
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.root = card()
                .addHeader(cardHeader()
                        .addTitle(cardTitle().textContent("Runtime"))
                        .addActions(cardActions()
                                .add(button().plain().icon(redo()).onClick((e, c) -> refresh()))))
                .element();
    }

    @Override
    public void refresh() {
        AddressTemplate mbean = AddressTemplate.of("core-service=platform-mbean");
        AddressTemplate osTmpl = mbean.append("type=operating-system");
        AddressTemplate runtimeTmpl = mbean.append("type=runtime");
        Operation osOp = new Operation.Builder(osTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation runtimeOp = new Operation.Builder(runtimeTmpl.resolve(statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
