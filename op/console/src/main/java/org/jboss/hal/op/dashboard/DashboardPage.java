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

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.elemento.router.Route;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.model.deployment.Deployments;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.clearInterval;
import static elemental2.dom.DomGlobal.setInterval;
import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.p;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.page.PageMainBody.pageMainBody;
import static org.patternfly.component.page.PageMainSection.pageMainSection;
import static org.patternfly.component.text.TextContent.textContent;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.layout.grid.Grid.grid;
import static org.patternfly.layout.grid.GridItem.gridItem;
import static org.patternfly.style.Brightness.light;

@Dependent
@Route("/")
public class DashboardPage implements Page {

    private static final double REFRESH_INTERVAL = 1_000;

    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final Deployments deployments;
    private final List<DashboardCard> cards;
    private double refreshHandle;

    @Inject
    public DashboardPage(Environment environment,
            StatementContext statementContext,
            Dispatcher dispatcher,
            Deployments deployments) {
        this.environment = environment;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.deployments = deployments;
        this.cards = new ArrayList<>();
    }

    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        DashboardCard deploymentCard = new DeploymentCard(environment, deployments);
        DashboardCard documentationCard = new DocumentationCard(environment);
        DashboardCard environmentCard = new ProductInfoCard(environment);
        DashboardCard logCard = new LogCard(environment, statementContext, dispatcher);
        DashboardCard runtimeCard = new RuntimeCard(statementContext, dispatcher);
        DashboardCard statusCard = new HealthCard(dispatcher);
        cards.addAll(asList(deploymentCard, documentationCard, environmentCard, logCard, runtimeCard,
                statusCard));

        List<HTMLElement> elements = asList(
                pageMainSection().limitWidth().background(light)
                        .addBody(pageMainBody()
                                .add(textContent()
                                        .add(title(1).text("WildFly Application Server"))
                                        .add(p().textContent("Dashboard"))))
                        .element(),
                pageMainSection().limitWidth()
                        .add(pageMainBody()
                                .add(grid().gutter()
                                        .addItem(gridItem().span(8)
                                                .add(environmentCard))
                                        .addItem(gridItem().span(4).rowSpan(2)
                                                .add(card().fullHeight()
                                                        .addTitle(cardTitle().textContent("Card"))
                                                        .addBody(cardBody().textContent("span = 4, rowSpan = 2"))))
                                        .addItem(gridItem().span(2).rowSpan(3)
                                                .add(card().fullHeight()
                                                        .addTitle(cardTitle().textContent("Card"))
                                                        .addBody(cardBody().textContent("span = 2, rowSpan = 3"))))
                                        .addItem(gridItem().span(2)
                                                .add(card()
                                                        .addTitle(cardTitle().textContent("Card"))
                                                        .addBody(cardBody().textContent("span = 2"))))
                                        .addItem(gridItem().span(4)
                                                .add(card()
                                                        .addTitle(cardTitle().textContent("Card"))
                                                        .addBody(cardBody().textContent("span = 4"))))
                                        .addItem(gridItem().span(2)
                                                .add(card()
                                                        .addTitle(cardTitle().textContent("Card"))
                                                        .addBody(cardBody().textContent("span = 2"))))
                                        .addItem(gridItem().span(2)
                                                .add(card()
                                                        .addTitle(cardTitle().textContent("Card"))
                                                        .addBody(cardBody().textContent("span = 2"))))
                                        .addItem(gridItem().span(2)
                                                .add(card()
                                                        .addTitle(cardTitle().textContent("Card"))
                                                        .addBody(cardBody().textContent("span = 2"))))
                                        .addItem(gridItem().span(4)
                                                .add(card()
                                                        .addTitle(cardTitle().textContent("Card"))
                                                        .addBody(cardBody().textContent("span = 4"))))
                                        .addItem(gridItem().span(2)
                                                .add(card()
                                                        .addTitle(cardTitle().textContent("Card"))
                                                        .addBody(cardBody().textContent("span = 2"))))
                                        .addItem(gridItem().span(4)
                                                .add(card()
                                                        .addTitle(cardTitle().textContent("Card"))
                                                        .addBody(cardBody().textContent("span = 4"))))
                                        .addItem(gridItem().span(4)
                                                .add(card()
                                                        .addTitle(cardTitle().textContent("Card"))
                                                        .addBody(cardBody().textContent("span = 4"))))))
                        .element());
        return elements;
    }

    @Override
    public void attach() {
        refresh();
        refreshHandle = setInterval(__ -> refresh(), REFRESH_INTERVAL);
    }

    @Override
    public void detach() {
        clearInterval(refreshHandle);
    }

    private void refresh() {
        for (DashboardCard card : cards) {
            card.refresh();
        }
    }
}
