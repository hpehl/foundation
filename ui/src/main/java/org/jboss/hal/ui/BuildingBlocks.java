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
import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.Description;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.meta.description.RestartMode;
import org.patternfly.component.list.List;
import org.patternfly.component.list.ListItem;
import org.patternfly.icon.PredefinedIcon;
import org.patternfly.style.Color;
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
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.icon.IconSets.fas.exclamationTriangle;
import static org.patternfly.icon.IconSets.fas.flask;
import static org.patternfly.icon.IconSets.fas.infoCircle;
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

    public static HTMLContainerBuilder<HTMLElement> attributeName(AttributeDescription attribute) {
        return strong()
                .textContent(attribute.name())
                .run(strong -> {
                    if (attribute.deprecation() != null) {
                        strong.css(halModifier(deprecated));
                    }
                });
    }

    public static HTMLContainerBuilder<HTMLDivElement> attributeDescription(AttributeDescription attribute) {
        Variable marginTop = componentVar(component(list), "li", "MarginTop");
        Variable marginLeft = componentVar(component(list), "nested", "MarginLeft");

        List infos = list().plain()
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
        if (deprecation != null) {
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
