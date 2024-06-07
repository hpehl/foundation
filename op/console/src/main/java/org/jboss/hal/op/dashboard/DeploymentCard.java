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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.hal.env.Environment;
import org.jboss.hal.model.deployment.Deployment;
import org.jboss.hal.model.deployment.DeploymentStatus;
import org.jboss.hal.model.deployment.Deployments;
import org.patternfly.component.card.CardBody;
import org.patternfly.component.card.CardTitle;
import org.patternfly.icon.PredefinedIcon;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.model.deployment.DeploymentStatus.FAILED;
import static org.jboss.hal.model.deployment.DeploymentStatus.OK;
import static org.jboss.hal.model.deployment.DeploymentStatus.STOPPED;
import static org.jboss.hal.op.skeleton.Domain.domainModeNotSupported;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardActions.cardActions;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardFooter.cardFooter;
import static org.patternfly.component.card.CardHeader.cardHeader;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.divider.Divider.divider;
import static org.patternfly.component.divider.DividerType.hr;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.icon.IconSets.fas.checkCircle;
import static org.patternfly.icon.IconSets.fas.pauseCircle;
import static org.patternfly.icon.IconSets.fas.question;
import static org.patternfly.icon.IconSets.fas.redo;
import static org.patternfly.icon.IconSets.fas.timesCircle;
import static org.patternfly.layout.flex.Display.inlineFlex;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.style.Orientation.vertical;
import static org.patternfly.style.Size.xs;
import static org.patternfly.style.Variable.globalVar;

class DeploymentCard implements DashboardCard {

    private final Environment environment;
    private final Deployments deployments;
    private final HTMLElement root;
    private final CardTitle cardTitle;
    private final CardBody cardBody;

    DeploymentCard(Environment environment, Deployments deployments) {
        this.environment = environment;
        this.deployments = deployments;
        this.root = card()
                .addHeader(cardHeader()
                        .addActions(cardActions()
                                .add(button().plain().icon(redo()).onClick((e, c) -> refresh()))))
                .addTitle(cardTitle = cardTitle().style("text-align", "center"))
                .addBody(cardBody = cardBody().style("text-align", "center"))
                .addFooter(cardFooter()
                        .add(a("#").textContent("All deployments")))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void refresh() {
        removeChildrenFrom(cardTitle);
        removeChildrenFrom(cardBody);
        if (environment.standalone()) {
            deployments.readStandaloneDeployments().then(deployments -> {
                if (deployments.isEmpty()) {
                    cardBody.add(emptyState().size(xs)
                            .addHeader(emptyStateHeader().text("No deployments"))
                            .addBody(emptyStateBody().textContent("This server contains no deployments.")));
                } else if (deployments.size() == 1) {
                    cardTitle.textContent("1 Deployment");
                    cardBody.add(status(deployments.get(0).status()));
                } else {
                    cardTitle.textContent(deployments.size() + " Deployments");
                    Map<Integer, PredefinedIcon> status = status(deployments);
                    if (status.size() == 1) {
                        cardBody.add(status.values().iterator().next());
                    } else {
                        cardBody.add(flex().display(inlineFlex)
                                .run(flex -> {
                                    for (Iterator<Map.Entry<Integer, PredefinedIcon>> iterator = status.entrySet().iterator();
                                            iterator.hasNext(); ) {
                                        Map.Entry<Integer, PredefinedIcon> entry = iterator.next();
                                        flex.add(flex().spaceItems(sm)
                                                .add(div().add(entry.getValue()))
                                                .add(div().textContent(entry.getKey().toString())));
                                        if (iterator.hasNext()) {
                                            flex.add(divider(hr).orientation(vertical));
                                        }
                                    }
                                }));
                    }
                }
                return null;
            });
        } else {
            // TODO Add support for domain mode
            cardBody.add(domainModeNotSupported(xs));
        }
    }

    private Map<Integer, PredefinedIcon> status(List<Deployment> deployments) {
        int[] status = new int[4];
        Map<Integer, PredefinedIcon> result = new HashMap<>();
        for (Deployment deployment : deployments) {
            switch (deployment.status()) {
                case OK:
                    status[0]++;
                    break;
                case STOPPED:
                    status[1]++;
                    break;
                case FAILED:
                    status[2]++;
                    break;
                case UNDEFINED:
                    status[3]++;
            }
        }
        if (status[0] > 0) {
            result.put(status[0], status(OK));
        }
        if (status[1] > 0) {
            result.put(status[1], status(STOPPED));
        }
        if (status[2] > 0) {
            result.put(status[2], status(FAILED));
        }
        if (status[3] > 0) {
            result.put(status[3], status(FAILED));
        }
        return result;
    }

    private PredefinedIcon status(DeploymentStatus status) {
        switch (status) {
            case OK:
                return checkCircle().attr("color", globalVar("success-color", "100").name);
            case FAILED:
                return timesCircle().attr("color", globalVar("danger-color", "100").name);
            case STOPPED:
                return pauseCircle();
            case UNDEFINED:
            default:
                return question().attr("color", globalVar("warning-color", "100").name);
        }
    }
}
