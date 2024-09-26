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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.HasElement;
import org.jboss.elemento.Id;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.HalDataset;
import org.jboss.hal.ui.LabelBuilder;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.button.Button;
import org.patternfly.component.codeblock.CodeBlock;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.label.Label;
import org.patternfly.component.list.DescriptionList;
import org.patternfly.component.list.DescriptionListGroup;
import org.patternfly.component.list.DescriptionListTerm;
import org.patternfly.component.switch_.Switch;
import org.patternfly.core.ObservableValue;
import org.patternfly.core.Tuple;
import org.patternfly.filter.Filter;
import org.patternfly.style.Size;
import org.patternfly.style.Variables;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.clearTimeout;
import static elemental2.dom.DomGlobal.setTimeout;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.isAttached;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
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
import static org.jboss.hal.resources.HalClasses.filtered;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.resourceView;
import static org.jboss.hal.resources.HalClasses.undefined;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescription;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.modelbrowser.ModelBrowser.dispatchSelectEvent;
import static org.jboss.hal.ui.resource.ResourceToolbar.resourceToolbar;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.codeblock.CodeBlock.codeBlock;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateActions.emptyStateActions;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
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
import static org.patternfly.core.Attributes.role;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.core.Roles.button;
import static org.patternfly.core.Timeouts.LOADING_TIMEOUT;
import static org.patternfly.core.Tuple.tuple;
import static org.patternfly.icon.IconSets.fas.ban;
import static org.patternfly.icon.IconSets.fas.exclamationCircle;
import static org.patternfly.icon.IconSets.fas.link;
import static org.patternfly.icon.IconSets.fas.search;
import static org.patternfly.style.Breakpoint._2xl;
import static org.patternfly.style.Breakpoint.default_;
import static org.patternfly.style.Breakpoint.lg;
import static org.patternfly.style.Breakpoint.md;
import static org.patternfly.style.Breakpoint.sm;
import static org.patternfly.style.Breakpoint.xl;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.descriptionList;
import static org.patternfly.style.Classes.helpText;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.text;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Color.grey;
import static org.patternfly.style.Variable.globalVar;
import static org.patternfly.style.Variable.utilVar;

// TODO Implement resolve all expressions, reset, and edit actions
public class ResourceView implements HasElement<HTMLElement, ResourceView> {

    // ------------------------------------------------------ factory

    public static ResourceView resourceView(UIContext uic, Metadata metadata) {
        return new ResourceView(uic, metadata);
    }

    public static ResourceView resourceView(UIContext uic, Metadata metadata, ModelNode resource) {
        ResourceView resourceView = new ResourceView(uic, metadata);
        resourceView.show(resource);
        return resourceView;
    }

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(ResourceView.class.getName());
    private static final String RESOURCE_ATTRIBUTE_KEY = "resourceView.ra";
    private final UIContext uic;
    private final Metadata metadata;
    private final LabelBuilder labelBuilder;
    private final List<String> attributes;
    private final Map<String, UpdateValueFn> updateValueFunctions;
    private final Filter<ResourceAttribute> filter;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final ResourceToolbar toolbar;
    private final HTMLElement viewContainer;
    private final HTMLElement root;
    private DescriptionList dl;
    private EmptyState noAttributes;
    private boolean shown;

    ResourceView(UIContext uic, Metadata metadata) {
        this.uic = uic;
        this.metadata = metadata;
        this.labelBuilder = new LabelBuilder();
        this.attributes = new ArrayList<>();
        this.updateValueFunctions = new HashMap<>();
        this.filter = new ResourceFilter().onChange(this::onFilterChanged);
        this.visible = ov(0);
        this.total = ov(0);
        this.shown = false;

        this.root = div()
                .add(toolbar = resourceToolbar(filter, visible, total))
                .add(viewContainer = div().element())
                .element();
        setVisible(toolbar, false);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ builder

    public ResourceView attributes(Iterable<String> attributes) {
        for (String attribute : attributes) {
            this.attributes.add(attribute);
        }
        return this;
    }

    @Override
    public ResourceView that() {
        return this;
    }

    // ------------------------------------------------------ api

    public void show(ModelNode resource) {
        shown = false;
        if (metadata.isDefined()) {
            if (valid(resource)) {
                removeChildrenFrom(viewContainer);
                updateValueFunctions.clear();
                dl = descriptionList().css(halComponent(resourceView))
                        .horizontal()
                        .horizontalTermWidth(breakpoints(
                                default_, "12ch",
                                sm, "15ch",
                                md, "18ch",
                                lg, "23ch",
                                xl, "25ch",
                                _2xl, "28ch"));
                List<ResourceAttribute> resourceAttributes = resourceAttributes(resource);
                visible.set(resourceAttributes.size());
                total.set(resourceAttributes.size());
                for (ResourceAttribute ra : resourceAttributes) {
                    DescriptionListTerm term = label(ra);
                    Tuple<HTMLElement, UpdateValueFn> tuple = value(ra);
                    dl.addItem(descriptionListGroup(Id.build(ra.name, "group"))
                            .store(RESOURCE_ATTRIBUTE_KEY, ra)
                            .addTerm(term)
                            .addDescription(descriptionListDescription()
                                    .add(tuple.key)));
                    updateValueFunctions.put(ra.name, tuple.value);
                    tuple.value.update(ra.value);
                }
                setVisible(toolbar, true);
                viewContainer.append(dl.element());
                shown = true;
            } else {
                empty();
            }
        } else {
            error();
        }
    }

    public void update(ModelNode resource) {
        if (metadata.isDefined()) {
            if (!shown) {
                show(resource);
            } else if (valid(resource)) {
                List<ResourceAttribute> resourceAttributes = resourceAttributes(resource);
                visible.set(resourceAttributes.size());
                total.set(resourceAttributes.size());
                for (ResourceAttribute ra : resourceAttributes) {
                    UpdateValueFn updateAttribute = updateValueFunctions.get(ra.name);
                    if (updateAttribute != null) {
                        updateAttribute.update(ra.value);
                    } else {
                        logger.warn("Unable to update attribute %s. No update function found", ra.name);
                    }
                }
            } else {
                empty();
            }
        } else {
            error();
        }
    }

    // ------------------------------------------------------ internal / status

    private boolean valid(ModelNode resource) {
        return resource != null && resource.isDefined() && !resource.asPropertyList().isEmpty();
    }

    private void error() {
        setVisible(toolbar, false);
        removeChildrenFrom(viewContainer);
        viewContainer.append(emptyState().size(Size.sm)
                .addHeader(emptyStateHeader()
                        .icon(exclamationCircle(), globalVar("danger-color", "100"))
                        .text("No metadata"))
                .addBody(emptyStateBody()
                        .textContent("Unable to view resource: No metadata found!"))
                .element());
    }

    private void empty() {
        setVisible(toolbar, false);
        removeChildrenFrom(viewContainer);
        viewContainer.append(emptyState().size(Size.sm)
                .addHeader(emptyStateHeader()
                        .icon(ban())
                        .text("No attributes"))
                .addBody(emptyStateBody()
                        .textContent("This resource contains no attributes."))
                .element());
    }

    private void noAttributes() {
        if (noAttributes == null) {
            noAttributes = emptyState().size(Size.sm)
                    .addHeader(emptyStateHeader()
                            .icon(search())
                            .text("No results found"))
                    .addBody(emptyStateBody()
                            .textContent("No results match the filter criteria. Clear all filters and try again."))
                    .addFooter(emptyStateFooter()
                            .addActions(emptyStateActions()
                                    .add(button("Clear all filters").link()
                                            .onClick((event, component) -> filter.resetAll()))));
        }
        if (!isAttached(noAttributes)) {
            viewContainer.append(noAttributes.element());
        }
    }

    // ------------------------------------------------------ internal / resource attributes

    private List<ResourceAttribute> resourceAttributes(ModelNode resource) {
        List<ResourceAttribute> resourceAttributes = new ArrayList<>();
        if (attributes.isEmpty()) {
            // collect all properties (including nested, record-like properties)
            for (Property property : resource.asPropertyList()) {
                String name = property.getName();
                ModelNode value = property.getValue();
                AttributeDescription description = metadata.resourceDescription().attributes().get(name);
                if (description.simpleValueType()) {
                    AttributeDescriptions nestedDescriptions = description.valueTypeAttributeDescriptions();
                    for (AttributeDescription nestedDescription : nestedDescriptions) {
                        ModelNode nestedValue = ModelNodeHelper.nested(resource, nestedDescription.name());
                        resourceAttributes.add(new ResourceAttribute(nestedDescription.name(), nestedValue, nestedDescription));
                    }
                } else {
                    resourceAttributes.add(new ResourceAttribute(name, value, description));
                }
            }
        } else {
            // collect only the specified attributes (which can be nested)
            for (String attribute : attributes) {
                if (attribute.contains(".")) {
                    // TODO Support nested attributes
                } else {
                    ModelNode value = resource.get(attribute);
                    AttributeDescription description = metadata.resourceDescription().attributes().get(attribute);
                    resourceAttributes.add(new ResourceAttribute(attribute, value, description));
                }
            }
        }
        return resourceAttributes;
    }

    private DescriptionListTerm label(ResourceAttribute ra) {
        DescriptionListTerm term;
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
                        .attr(role, button)
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
            }

            // only the top level attribute is stability-labeled
            if (uic.environment().highlightStability(metadata.resourceDescription().stability(), ra.description.stability())) {
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
            if (ra.description.deprecation().isDefined()) {
                term.delegate().classList.add(halModifier(deprecated));
            }

        } else {
            term = descriptionListTerm(labelBuilder.label(ra.name));
        }
        return term;
    }

    private Tuple<HTMLElement, UpdateValueFn> value(ResourceAttribute ra) {
        HTMLElement element;
        UpdateValueFn fn;

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
                                    Button button = button()
                                            .link()
                                            .inline()
                                            .progress(false, "Search for capability")
                                            .data(HalDataset.capabilityReference, capability)
                                            .data(HalDataset.capabilityValue, "")
                                            .onClick((__, btn) -> findCapability(btn));
                                    element = span()
                                            .add(tooltip(button.element(), "Follow capability reference " + capability))
                                            .add(button)
                                            .element();
                                    fn = value -> {
                                        button.data(HalDataset.capabilityValue, value.asString());
                                        button.text(value.asString());
                                    };
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
            element = span().css(halComponent(resourceView, undefined)).element();
            fn = value -> element.textContent = value.asString();
        }
        return tuple(element, fn);
    }

    private void findCapability(Button button) {
        String capability = button.element().dataset.get(HalDataset.capabilityReference);
        String value = button.element().dataset.get(HalDataset.capabilityValue);
        if (capability != null && !capability.isEmpty() && value != null && !value.isEmpty()) {
            double handle = setTimeout(__ -> button.startProgress(), LOADING_TIMEOUT);
            uic.capabilityRegistry().findReference(capability, value)
                    .then(template -> {
                        clearTimeout(handle);
                        button.stopProgress();
                        if (template != null) {
                            dispatchSelectEvent(element(), template);
                        } else {
                            // TODO Show an alert!
                            logger.error("Unable to find capability %s for value %s", capability, value);
                        }
                        return null;
                    });
        } else {
            logger.error("Unable to find capability. Dataset properties for capability and/or value not found on %s",
                    button.element());
        }
    }

    // ------------------------------------------------------ internal / filter

    private void onFilterChanged(Filter<ResourceAttribute> filter, String origin) {
        logger.debug("Filter attributes: %s", filter);
        int matchingItems;
        if (dl != null) {
            if (filter.defined()) {
                matchingItems = 0;
                for (DescriptionListGroup dlg : dl.items()) {
                    ResourceAttribute ra = dlg.get(RESOURCE_ATTRIBUTE_KEY);
                    if (ra != null) {
                        boolean match = filter.match(ra);
                        dlg.classList().toggle(halModifier(filtered), !match);
                        if (match) {
                            matchingItems++;
                        }
                    }
                }
                if (matchingItems == 0) {
                    noAttributes();
                } else {
                    failSafeRemoveFromParent(noAttributes);
                }
            } else {
                matchingItems = total.get();
                failSafeRemoveFromParent(noAttributes);
                dl.items().forEach(dlg -> dlg.classList().remove(halModifier(filtered)));
            }
            visible.set(matchingItems);
        }
    }
}
