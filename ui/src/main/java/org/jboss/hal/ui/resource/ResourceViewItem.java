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
package org.jboss.hal.ui.resource;

import java.util.List;

import org.jboss.elemento.Id;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.LabelBuilder;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.codeblock.CodeBlock;
import org.patternfly.component.label.Label;
import org.patternfly.component.list.DescriptionListGroup;
import org.patternfly.component.list.DescriptionListTerm;
import org.patternfly.component.switch_.Switch;
import org.patternfly.core.Roles;
import org.patternfly.core.Tuple;
import org.patternfly.style.Variables;

import elemental2.dom.HTMLElement;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.wrapHtmlContainer;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNIT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.jboss.hal.dmr.ModelType.BOOLEAN;
import static org.jboss.hal.dmr.ModelType.EXPRESSION;
import static org.jboss.hal.dmr.ModelType.LIST;
import static org.jboss.hal.dmr.ModelType.OBJECT;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.resourceView;
import static org.jboss.hal.resources.HalClasses.undefined;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescription;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.codeblock.CodeBlock.codeBlock;
import static org.patternfly.component.label.LabelGroup.labelGroup;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.component.popover.Popover.popover;
import static org.patternfly.component.popover.PopoverBody.popoverBody;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.core.Attributes.role;
import static org.patternfly.core.Tuple.tuple;
import static org.patternfly.icon.IconSets.fas.link;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.descriptionList;
import static org.patternfly.style.Classes.helpText;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.text;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Color.grey;
import static org.patternfly.style.Variable.globalVar;
import static org.patternfly.style.Variable.utilVar;

class ResourceViewItem {

    static final String RESOURCE_ATTRIBUTE_KEY = "resourceViewItem";
    private static final Logger logger = Logger.getLogger(ResourceViewItem.class.getName());

    static ResourceViewItem resourceViewItem(UIContext uic, Metadata metadata, ResourceAttribute ra) {
        DescriptionListTerm dlt = term(uic, metadata, ra);
        Tuple<HTMLElement, UpdateValueFn> tuple = elementFn(uic, ra);
        DescriptionListGroup dlg = descriptionListGroup(Id.build(ra.name, "group"))
                .store(RESOURCE_ATTRIBUTE_KEY, ra)
                .addTerm(dlt)
                .addDescription(descriptionListDescription().add(tuple.key));
        return new ResourceViewItem(dlg, tuple.value);
    }

    private static DescriptionListTerm term(UIContext uic, Metadata metadata, ResourceAttribute ra) {
        DescriptionListTerm term;
        LabelBuilder labelBuilder = new LabelBuilder();
        if (ra.description != null) {
            if (ra.description.nested()) {
                // <unstable>
                // == If the internal DOM of DescriptionListTerm changes, this will no longer work ==
                // By default, DescriptionListTerm supports only one text element. But in this case we
                // want to have one for the parent and one for the nested attribute description.
                // So we set up the internals of DescriptionListTerm manually.
                AttributeDescription parentDescription = ra.description.parent();
                AttributeDescription nestedDescription = ra.description;
                String parentLabel = labelBuilder.label(parentDescription.name());
                String nestedLabel = labelBuilder.label(ra.name);
                term = descriptionListTerm(parentLabel)
                        .help(popover()
                                .css(util("min-width"))
                                .style(utilVar("min-width", Variables.MinWidth).name, "40ch")
                                .addHeader(parentLabel)
                                .addBody(popoverBody()
                                        .add(attributeDescription(parentDescription))));
                HTMLElement nestedTextElement = span()
                        .css(component(descriptionList, text), modifier(helpText))
                        .attr(role, Roles.button)
                        .attr("type", "button")
                        .apply(element -> element.tabIndex = 0)
                        .textContent(nestedLabel)
                        .element();
                popover()
                        .css(util("min-width"))
                        .style(utilVar("min-width", Variables.MinWidth).name, "40ch")
                        .addHeader(nestedLabel)
                        .addBody(popoverBody()
                                .add(attributeDescription(nestedDescription)))
                        .trigger(nestedTextElement)
                        .appendToBody();
                wrapHtmlContainer(term.element())
                        .style("flex-wrap", "wrap")
                        .style("gap", globalVar("spacer", "xs").asVar())
                        .add("/")
                        .add(nestedTextElement);
                // </unstable>
            } else {
                String label = labelBuilder.label(ra.name);
                term = descriptionListTerm(label);
                term.help(popover()
                        .css(util("min-width"))
                        .style(utilVar("min-width", Variables.MinWidth).name, "40ch")
                        .addHeader(label)
                        .addBody(popoverBody()
                                .add(attributeDescription(ra.description))));

                // only the top level attribute is stability-labeled
                if (uic.environment()
                        .highlightStability(metadata.resourceDescription().stability(), ra.description.stability())) {
                    // <unstable>
                    // == If the internal DOM of DescriptionListTerm changes, this will no longer work ==
                    // DescriptionListTerm implements ElementDelegate and delegates to the internal text element.
                    // That's why we must use term.element.appendChild() instead of term.add() to add the
                    // stability label after the text element instead of into the text element. Then we must
                    // reset the font weight to normal (DescriptionListTerm uses bold)
                    term.element().style.setProperty("align-items", "center");
                    term.element().appendChild(stabilityLabel(ra.description.stability()).compact()
                            .style("align-self", "baseline")
                            .css(util("ml-sm"), util("font-weight-normal"))
                            .element());
                    // </unstable>
                }
            }
            if (ra.description.deprecation().isDefined()) {
                term.delegate().classList.add(halModifier(deprecated));
            }
        } else {
            term = descriptionListTerm(labelBuilder.label(ra.name));
        }
        return term;
    }

    private static Tuple<HTMLElement, UpdateValueFn> elementFn(UIContext uic, ResourceAttribute ra) {
        UpdateValueFn fn;
        HTMLElement element;

        // TODO Implement default values and sensitive
        if (ra.value.isDefined()) {
            if (ra.value.getType() == EXPRESSION) {
                HTMLElement resolveButton = button().plain().inline().icon(link()).element();
                HTMLElement expressionElement = span().element();
                element = span()
                        .add(tooltip(resolveButton, "Resolve expression (nyi)"))
                        .add(expressionElement)
                        .add(resolveButton).element();
                fn = value -> expressionElement.textContent = value.asString();
            } else {
                if (ra.description != null) {
                    if (ra.description.hasDefined(TYPE)) {
                        ModelType type = ra.description.get(TYPE).asType();
                        if (type == BOOLEAN) {
                            String unique = Id.unique(ra.name);
                            Switch switch_ = Switch.switch_(unique, unique)
                                    .ariaLabel(ra.name)
                                    .checkIcon()
                                    .readonly();
                            element = switch_.element();
                            fn = value -> switch_.value(value.asBoolean());
                        } else if (type.simple()) {
                            String unit = ra.description.hasDefined(UNIT) ? ra.description.get(UNIT)
                                    .asString() : null;
                            if (unit != null) {
                                HTMLElement valueElement = span().element();
                                HTMLElement unitElement = span().css(halComponent(resourceView, HalClasses.unit))
                                        .textContent(unit)
                                        .element();
                                element = span().add(valueElement).add(unitElement).element();
                                fn = value -> valueElement.textContent = value.asString();
                            } else if (ra.description.hasDefined(ALLOWED)) {
                                List<String> allowed = ra.description.get(ALLOWED)
                                        .asList()
                                        .stream()
                                        .map(ModelNode::asString)
                                        .collect(toList());
                                allowed.remove(ra.value.asString());
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
                                if (ra.description.hasDefined(CAPABILITY_REFERENCE)) {
                                    String capability = ra.description.get(CAPABILITY_REFERENCE).asString();
                                    CapabilityReferenceLink crl = new CapabilityReferenceLink(uic, capability);
                                    element = crl.element();
                                    fn = value -> crl.assignValue(value.asString());
                                } else {
                                    element = span().element();
                                    fn = value -> element.textContent = value.asString();
                                }
                            }
                        } else if (type == LIST) {
                            ModelType valueType = ra.description.has(VALUE_TYPE) &&
                                    ra.description.get(VALUE_TYPE).getType() != OBJECT
                                    ? ra.description.get(VALUE_TYPE).asType()
                                    : null;
                            if (valueType != null && valueType.simple()) {
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
                                fn = value -> codeBlock.code(value.toJSONString().replace("\\/", "/"));
                            }
                        } else if (type == OBJECT) {
                            CodeBlock codeBlock = codeBlock().truncate(5);
                            element = codeBlock.element();
                            fn = value -> codeBlock.code(value.toJSONString().replace("\\/", "/"));
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
            element = span().css(halComponent(resourceView, undefined)).element();
            fn = value -> element.textContent = value.asString();
        }
        return tuple(element, fn);
    }

    final DescriptionListGroup dlg;
    final UpdateValueFn update;

    ResourceViewItem(DescriptionListGroup dlg, UpdateValueFn update) {
        this.dlg = dlg;
        this.update = update;
    }
}
