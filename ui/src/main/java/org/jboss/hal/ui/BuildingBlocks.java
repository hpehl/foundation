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
package org.jboss.hal.ui;

import java.util.Iterator;
import java.util.function.Supplier;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.Deprecation;
import org.jboss.hal.meta.description.Description;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.meta.description.RestartMode;
import org.patternfly.component.list.ListItem;
import org.patternfly.component.table.Tr;
import org.patternfly.filter.Filter;
import org.patternfly.icon.PredefinedIcon;
import org.patternfly.layout.flex.Flex;
import org.patternfly.style.Color;
import org.patternfly.style.Size;
import org.patternfly.style.Variable;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.i;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALTERNATIVES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXPRESSIONS_ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESTART_REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;
import static org.jboss.hal.env.Stability.EXPERIMENTAL;
import static org.jboss.hal.env.Stability.PREVIEW;
import static org.jboss.hal.meta.description.RestartMode.UNKNOWN;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.icon.IconSets.fas.exclamationTriangle;
import static org.patternfly.icon.IconSets.fas.flask;
import static org.patternfly.icon.IconSets.fas.infoCircle;
import static org.patternfly.icon.IconSets.fas.search;
import static org.patternfly.layout.bullseye.Bullseye.bullseye;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.list;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Color.blue;
import static org.patternfly.style.Color.gold;
import static org.patternfly.style.Color.red;
import static org.patternfly.style.Variable.componentVar;

/** Contains various UI-related methods used across the UI module. */
public class BuildingBlocks {

    // ------------------------------------------------------ attributes

    public static Flex attributeName(AttributeDescription attribute, Supplier<Boolean> stabilityCheck) {
        return attributeName(attribute, false, stabilityCheck);
    }

    public static Flex attributeName(AttributeDescription attribute, boolean compact, Supplier<Boolean> stabilityCheck) {
        HTMLContainerBuilder<HTMLElement> name = strong()
                .textContent(attribute.name())
                .run(element -> {
                    if (attribute.deprecation().isDefined()) {
                        element.css(halModifier(deprecated));
                    }
                });
        if (stabilityCheck.get()) {
            return flex().alignItems(center).spaceItems(sm)
                    .addItem(flexItem().add(name))
                    .addItem(flexItem().add(stabilityLabel(attribute.stability()).compact(compact)));
        } else {
            return flex().add(name);
        }
    }

    public static HTMLContainerBuilder<HTMLDivElement> attributeDescription(AttributeDescription attribute) {
        Variable marginTop = componentVar(component(list), "li", "MarginTop");
        Variable marginLeft = componentVar(component(list), "nested", "MarginLeft");

        org.patternfly.component.list.List infos = list().plain()
                .css(util("mt-sm"))
                .style(marginTop.name, 0)
                .style(marginLeft.name, 0);
        if (attribute.get(REQUIRED).asBoolean(false)) {
            infos.add(listItem().text("Required."));
        }
        if (attribute.hasDefined(CAPABILITY_REFERENCE)) {
            infos.addItem(listItem()
                    .add("References the capability ")
                    .add(code().textContent(attribute.get(CAPABILITY_REFERENCE).asString()))
                    .add("."));
        }
        if (attribute.get(EXPRESSIONS_ALLOWED).asBoolean(false)) {
            infos.add(listItem()
                    .add("Supports expressions."));
        }
        if (attribute.hasDefined(UNIT)) {
            infos.addItem(listItem()
                    .add("Uses ")
                    .add(i().textContent(attribute.get(UNIT).asString()))
                    .add(" as unit."));
        }
        if (attribute.hasDefined(REQUIRES)) {
            infos.addItem(listItem()
                    .add("Requires ")
                    .run(listItem -> enumerate(listItem, attribute.get(REQUIRES).asList())));
        }
        if (attribute.hasDefined(ALTERNATIVES)) {
            infos.addItem(listItem()
                    .add("Mutually exclusive to ")
                    .run(listItem -> enumerate(listItem, attribute.get(ALTERNATIVES).asList())));
        }
        if (attribute.hasDefined(RESTART_REQUIRED)) {
            RestartMode restartMode = asEnumValue(attribute, RESTART_REQUIRED, RestartMode::valueOf, UNKNOWN);
            if (restartMode != UNKNOWN) {
                String text = "";
                switch (restartMode) {
                    case ALL_SERVICES:
                        text = "A modification requires a restart of all services, but does not require a full JVM restart.";
                        break;
                    case JVM:
                        text = "A modification requires a full JVM restart.";
                        break;
                    case NO_SERVICES:
                        text = "A modification doesn't require a restart.";
                        break;
                    case RESOURCE_SERVICES:
                        text = "A modification requires a restart of services, associated with the attribute's resource, but does not require a restart of all services or a full JVM restart.";
                        break;
                }
                infos.addItem(listItem().textContent(text));
            }
        }

        return description(attribute).run(description -> {
            if (!infos.isEmpty()) {
                description.add(small().add(infos));
            }
        });
    }

    private static HTMLContainerBuilder<HTMLDivElement> description(Description description) {
        HTMLContainerBuilder<HTMLDivElement> div = div();
        div.add(div().textContent(description.description()));
        Deprecation deprecation = description.deprecation();
        if (deprecation.isDefined()) {
            div.add(div().css(util("mt-sm"))
                    .add("Deprecated since " + deprecation.since().toString())
                    .add(br())
                    .add("Reason: " + deprecation.reason()));
        }
        return div;
    }

    private static void enumerate(ListItem listItem, java.util.List<ModelNode> values) {
        for (Iterator<ModelNode> iterator = values.iterator(); iterator.hasNext(); ) {
            ModelNode value = iterator.next();
            listItem.add(code().textContent(value.asString()));
            if (iterator.hasNext()) {
                listItem.add(", ");
            }
        }
    }

    // ------------------------------------------------------ operations

    public static HTMLContainerBuilder<HTMLDivElement> operationDescription(OperationDescription operation) {
        return description(operation);
    }

    // ------------------------------------------------------ empty

    public static <T> Tr emptyRow(int colspan, Filter<T> filter) {
        return tr(Id.unique("empty"))
                .addItem(td().colSpan(colspan)
                        .add(bullseye()
                                .add(emptyState().size(Size.sm)
                                        .addHeader(emptyStateHeader()
                                                .icon(search())
                                                .text("No results found"))
                                        .addBody(emptyStateBody()
                                                .textContent(
                                                        "No results match the filter criteria. Clear all filters and try again."))
                                        .addFooter(emptyStateFooter()
                                                .addActions(emptyStateActions()
                                                        .add(button("Clear all filters").link()
                                                                .onClick((event, component) -> filter.resetAll())))))));
    }

    // ------------------------------------------------------ stability

    public static Color stabilityColor(Stability stability) {
        if (stability == EXPERIMENTAL) {
            return red;
        } else if (stability == PREVIEW) {
            return gold;
        }
        return blue;
    }

    public static PredefinedIcon stabilityIcon(Stability stability) {
        if (stability == EXPERIMENTAL) {
            return flask();
        } else if (stability == PREVIEW) {
            return exclamationTriangle();
        }
        return infoCircle();
    }

    public static Supplier<PredefinedIcon> stabilityIconSupplier(Stability stability) {
        return () -> stabilityIcon(stability);
    }
}
