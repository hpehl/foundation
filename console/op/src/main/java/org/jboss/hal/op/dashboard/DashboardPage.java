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

import jakarta.inject.Inject;

import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Route;
import org.jboss.hal.env.Environment;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.p;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardTitle.cardTitle;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.component.page.PageMainBody.pageMainBody;
import static org.patternfly.component.page.PageMainSection.pageMainSection;
import static org.patternfly.component.text.TextContent.textContent;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.layout.grid.Grid.grid;
import static org.patternfly.layout.grid.GridItem.gridItem;
import static org.patternfly.style.Breakpoint.default_;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Brightness.light;

@Route("/")
public class DashboardPage implements Page {

    @Inject Environment environment;

    @Override
    public Iterable<HTMLElement> elements() {
        return asList(pageMainSection().limitWidth().background(light)
                        .addBody(pageMainBody()
                                .add(textContent()
                                        .add(title(1).text("WildFly Application Server"))
                                        .add(p().textContent("Dashboard"))))
                        .element(),
                pageMainSection().limitWidth()
                        .add(pageMainBody()
                                .add(grid().gutter()
                                        .addItem(gridItem().span(8)
                                                .add(card()
                                                        .addTitle(cardTitle().textContent("Environment:"))
                                                        .addBody(cardBody()
                                                                .add(descriptionList().columns(breakpoints(default_, 2))
                                                                        .addGroup(descriptionListGroup()
                                                                                .addTerm(descriptionListTerm("ID"))
                                                                                .addDescription(descriptionListDescription(
                                                                                        environment.applicationId()))
                                                                                .addTerm(descriptionListTerm("Name"))
                                                                                .addDescription(descriptionListDescription(
                                                                                        environment.applicationName())))
                                                                        .addGroup(descriptionListGroup()
                                                                                .addTerm(descriptionListTerm("Version"))
                                                                                .addDescription(descriptionListDescription(
                                                                                        environment.applicationVersion()
                                                                                                .toString()))
                                                                                .addTerm(descriptionListTerm("Mode"))
                                                                                .addDescription(descriptionListDescription(
                                                                                        environment.buildType().name())))))))
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
    }
}
