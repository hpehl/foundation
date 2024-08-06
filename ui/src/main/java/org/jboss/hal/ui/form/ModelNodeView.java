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
package org.jboss.hal.ui.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.HasElement;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.LabelBuilder;
import org.patternfly.component.Severity;
import org.patternfly.component.codeblock.CodeBlock;
import org.patternfly.component.label.Label;
import org.patternfly.component.list.DescriptionList;
import org.patternfly.component.list.DescriptionListDescription;
import org.patternfly.component.list.DescriptionListTerm;
import org.patternfly.component.popover.PopoverBody;
import org.patternfly.component.switch_.Switch;
import org.patternfly.core.Tuple;

import elemental2.dom.HTMLElement;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.jboss.hal.dmr.ModelType.BIG_DECIMAL;
import static org.jboss.hal.dmr.ModelType.BIG_INTEGER;
import static org.jboss.hal.dmr.ModelType.BOOLEAN;
import static org.jboss.hal.dmr.ModelType.DOUBLE;
import static org.jboss.hal.dmr.ModelType.EXPRESSION;
import static org.jboss.hal.dmr.ModelType.INT;
import static org.jboss.hal.dmr.ModelType.LIST;
import static org.jboss.hal.dmr.ModelType.LONG;
import static org.jboss.hal.dmr.ModelType.OBJECT;
import static org.jboss.hal.dmr.ModelType.STRING;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.modelNodeView;
import static org.jboss.hal.resources.HalClasses.undefined;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.codeblock.CodeBlock.codeBlock;
import static org.patternfly.component.label.LabelGroup.labelGroup;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.popover.Popover.popover;
import static org.patternfly.component.popover.PopoverBody.popoverBody;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.core.Tuple.tuple;
import static org.patternfly.icon.IconSets.fas.link;
import static org.patternfly.style.Breakpoint._2xl;
import static org.patternfly.style.Breakpoint.default_;
import static org.patternfly.style.Breakpoint.lg;
import static org.patternfly.style.Breakpoint.md;
import static org.patternfly.style.Breakpoint.sm;
import static org.patternfly.style.Breakpoint.xl;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Color.grey;

public class ModelNodeView implements HasElement<HTMLElement, ModelNodeView>, IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static ModelNodeView modelNodeView(Metadata metadata) {
        return new ModelNodeView(metadata);
    }

    public static ModelNodeView modelNodeView(Metadata metadata, ModelNode modelNode) {
        ModelNodeView modelNodeView = new ModelNodeView(metadata);
        modelNodeView.show(modelNode);
        return modelNodeView;
    }

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(ModelNodeView.class.getName());
    private final Metadata metadata;
    private final LabelBuilder labelBuilder;
    private final List<String> attributes;
    private final Map<String, UpdateValueFn> updateValueFunctions;
    private final HTMLElement root;
    private boolean empty;

    ModelNodeView(Metadata metadata) {
        this.metadata = metadata;
        this.labelBuilder = new LabelBuilder();
        this.attributes = new ArrayList<>();
        this.updateValueFunctions = new HashMap<>();
        this.root = div().element();
        this.empty = true;
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ builder

    public ModelNodeView attributes(Iterable<String> attributes) {
        for (String attribute : attributes) {
            this.attributes.add(attribute);
        }
        return this;
    }

    @Override
    public ModelNodeView that() {
        return this;
    }

    // ------------------------------------------------------ api

    public void show(ModelNode modelNode) {
        if (metadata.empty) {
            error();
        } else {
            if (valid(modelNode)) {
                removeChildrenFrom(root);
                updateValueFunctions.clear();
                DescriptionList dl = descriptionList().css(halComponent(modelNodeView))
                        .horizontal()
                        .horizontalTermWidth(breakpoints(
                                default_, "12ch",
                                sm, "15ch",
                                md, "18ch",
                                lg, "23ch",
                                xl, "25ch",
                                _2xl, "28ch"));
                List<Property> properties = modelNode.asPropertyList();
                properties.sort(comparing(Property::getName));
                for (Property property : properties) {
                    String name = property.getName();
                    AttributeDescription attribute = metadata.resourceDescription.attributes().get(name);
                    dl.addItem(descriptionListGroup(Id.build(name, "group"))
                            .addTerm(label(name, attribute))
                            .addDescription(value(name, property.getValue(), attribute)));
                }
                root.append(dl.element());
                empty = false;
            } else {
                empty();
            }
        }
    }

    public void update(ModelNode modelNode) {
        if (metadata.empty) {
            error();
        } else {
            if (empty) {
                show(modelNode);
            } else if (valid(modelNode)) {
                for (Property property : modelNode.asPropertyList()) {
                    UpdateValueFn updateAttribute = updateValueFunctions.get(property.getName());
                    if (updateAttribute != null) {
                        updateAttribute.update(modelNode);
                    } else {
                        logger.warn("Unable to update attribute %s. No update function found", property.getName());
                    }
                }
            } else {
                empty();
            }
        }
    }

    // ------------------------------------------------------ internal

    private boolean valid(ModelNode modelNode) {
        return modelNode != null && modelNode.isDefined() && !modelNode.asPropertyList().isEmpty();
    }

    private void error() {
        removeChildrenFrom(root);
        root.append(alert(Severity.danger, "No metadata")
                .inline()
                .addDescription("Unable to view resource: No metadata found!")
                .element());
        empty = true;
        updateValueFunctions.clear();
    }

    private void empty() {
        removeChildrenFrom(root);
        root.append(alert(Severity.info, "No attributes")
                .inline()
                .addDescription("This resource contains no attributes.")
                .element());
        empty = true;
        updateValueFunctions.clear();
    }

    private DescriptionListTerm label(String name, AttributeDescription attribute) {
        String label = labelBuilder.label(name);
        DescriptionListTerm term = descriptionListTerm(label);
        if (attribute != null) {
            Deprecation deprecation = attribute.deprecation();
            PopoverBody popoverBody = popoverBody();
            if (deprecation != null) {
                term.css(halModifier(deprecated));
                popoverBody
                        .add(div().textContent(attribute.description()))
                        .add(div().css(util("mt-sm"))
                                .add("Deprecated since " + deprecation.since().toString())
                                .add(br())
                                .add("Reason: " + deprecation.reason()));
            } else {
                popoverBody.textContent(attribute.description());
            }
            term.help(popover()
                    .addHeader(label)
                    .addBody(popoverBody));
        }
        return term;
    }

    private DescriptionListDescription value(String name, ModelNode modelNode, AttributeDescription attribute) {
        Tuple<HTMLElement, UpdateValueFn> tuple = valueElement(name, modelNode, attribute);
        updateValueFunctions.put(name, tuple.value);
        return descriptionListDescription().add(tuple.key);
    }

    private Tuple<HTMLElement, UpdateValueFn> valueElement(String name, ModelNode modelNode, AttributeDescription attribute) {
        HTMLElement element;
        UpdateValueFn fn;

        // TODO Default values and sensitive
        if (modelNode.isDefined()) {
            if (modelNode.getType() == EXPRESSION) {
                HTMLElement resolveButton = button().plain().inline().icon(link()).element();
                HTMLElement expressionElement = span().element();
                element = span()
                        .add(tooltip(resolveButton, "Resolve expression (nyi)"))
                        .add(expressionElement)
                        .add(resolveButton).element();
                fn = value -> expressionElement.textContent = value.asString();
            } else {
                if (attribute != null) {
                    if (attribute.hasDefined(TYPE)) {
                        ModelType type = attribute.get(TYPE).asType();
                        ModelType valueType = (attribute.has(VALUE_TYPE) && attribute.get(VALUE_TYPE)
                                .getType() != OBJECT)
                                ? ModelType.valueOf(attribute.get(VALUE_TYPE).asString())
                                : null;
                        if (type == BOOLEAN) {
                            String unique = Id.unique(name);
                            Switch switch_ = Switch.switch_(unique, unique)
                                    .ariaLabel(name)
                                    .checkIcon()
                                    .readonly();
                            element = switch_.element();
                            fn = value -> switch_.value(value.asBoolean());
                        } else if (simpleType(type)) {
                            String unit = attribute.hasDefined(UNIT) ? attribute.get(UNIT).asString() : null;
                            if (unit != null) {
                                HTMLElement valueElement = span().element();
                                HTMLElement unitElement = span().css(halComponent(modelNodeView, HalClasses.unit))
                                        .textContent(unit)
                                        .element();
                                element = span().add(valueElement).add(unitElement).element();
                                fn = value -> valueElement.textContent = value.asString();
                            } else if (attribute.hasDefined(ALLOWED)) {
                                List<String> allowed = attribute.get(ALLOWED)
                                        .asList()
                                        .stream()
                                        .map(ModelNode::asString)
                                        .collect(toList());
                                allowed.remove(modelNode.asString());
                                allowed.sort(naturalOrder());
                                Label label = Label.label("", grey);
                                element = labelGroup()
                                        .numLabels(1)
                                        .collapsedText("Allowed values")
                                        .addItem(label)
                                        .addItems(allowed, a -> Label.label(a, grey).disabled())
                                        .element();
                                fn = value -> label.text(value.asString());
                            } else {
                                element = span().element();
                                fn = value -> element.textContent = value.asString();
                            }
                        } else if (type == LIST) {
                            if (simpleType(valueType)) {
                                element = div().element();
                                fn = value -> {
                                    removeChildrenFrom(element);
                                    List<String> values = value.asList().stream().map(ModelNode::asString).collect(toList());
                                    element.append(list().plain()
                                            .addItems(values, v -> listItem(Id.build(v, "value")).text(v))
                                            .element());
                                };
                            } else {
                                CodeBlock codeBlock = codeBlock().truncate(5);
                                element = codeBlock.element();
                                fn = value -> codeBlock.code(value.toJSONString());
                            }
                        } else if (type == OBJECT) {
                            CodeBlock codeBlock = codeBlock().truncate(5);
                            element = codeBlock.element();
                            fn = value -> codeBlock.code(value.toJSONString());
                        } else {
                            element = span().element();
                            fn = value -> element.textContent = value.asString();
                        }
                    } else {
                        element = span().element();
                        fn = value -> element.textContent = value.asString();
                    }
                } else {
                    element = span().element();
                    fn = value -> element.textContent = value.asString();
                }
            }
        } else {
            element = span().css(halComponent(modelNodeView, undefined)).element();
            fn = value -> element.textContent = value.asString();
        }

        fn.update(modelNode);
        return tuple(element, fn);
    }

    private boolean simpleType(ModelType type) {
        return type == BOOLEAN || type == BIG_DECIMAL || type == BIG_INTEGER || type == INT || type == LONG || type == DOUBLE || type == STRING;
    }
}
