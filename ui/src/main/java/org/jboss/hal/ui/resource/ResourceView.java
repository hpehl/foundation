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

import org.jboss.elemento.Attachable;
import org.jboss.elemento.HasElement;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.ui.UIContext;
import org.jboss.hal.ui.modelbrowser.NoMatch;
import org.patternfly.component.list.DescriptionList;
import org.patternfly.component.list.DescriptionListGroup;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;
import org.patternfly.style.Size;

import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;

import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.isAttached;
import static org.jboss.elemento.Elements.pre;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.resources.HalClasses.filtered;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.resourceView;
import static org.jboss.hal.ui.resource.ResourceToolbar.resourceToolbar;
import static org.jboss.hal.ui.resource.ResourceViewItem.RESOURCE_ATTRIBUTE_KEY;
import static org.jboss.hal.ui.resource.ResourceViewItem.resourceViewItem;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.icon.IconSets.fas.ban;
import static org.patternfly.icon.IconSets.fas.exclamationCircle;
import static org.patternfly.style.Breakpoint._2xl;
import static org.patternfly.style.Breakpoint.lg;
import static org.patternfly.style.Breakpoint.md;
import static org.patternfly.style.Breakpoint.sm;
import static org.patternfly.style.Breakpoint.xl;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Orientation.horizontal;
import static org.patternfly.style.Orientation.vertical;
import static org.patternfly.style.Variable.globalVar;

// TODO Implement resolve all expressions, reset, and edit actions
public class ResourceView implements HasElement<HTMLElement, ResourceView>, Attachable {

    // ------------------------------------------------------ factory

    public static ResourceView resourceView(UIContext uic, AddressTemplate template, Metadata metadata) {
        return new ResourceView(uic, template, metadata);
    }

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(ResourceView.class.getName());
    private final UIContext uic;
    private final Metadata metadata;
    private final List<String> attributes;
    private final Map<String, UpdateValueFn> updateFunctions;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final ResourceToolbar toolbar;
    private final NoMatch<ResourceAttribute> noMatch;
    private final HTMLElement viewContainer;
    private final HTMLElement root;
    private Operation operation;
    private DescriptionList dl;

    ResourceView(UIContext uic, AddressTemplate template, Metadata metadata) {
        Filter<ResourceAttribute> filter = new ResourceFilter().onChange(this::onFilterChanged);

        this.uic = uic;
        this.metadata = metadata;
        this.attributes = new ArrayList<>();
        this.updateFunctions = new HashMap<>();
        this.noMatch = new NoMatch<>(filter);
        this.visible = ov(0);
        this.total = ov(0);
        this.operation = new Operation.Builder(template.resolve(), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        this.root = div()
                .add(toolbar = resourceToolbar(this, filter, visible, total))
                .add(viewContainer = div().element())
                .element();

        setVisible(toolbar, false);
        Attachable.register(this, this);
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        init();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ builder

    public ResourceView operation(Operation operation) {
        if (operation != null) {
            this.operation = operation;
        } else {
            logger.error("Operation is null!");
        }
        return this;
    }

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

    // ------------------------------------------------------ init

    private void init() {
        if (metadata.isDefined()) {
            uic.dispatcher().execute(operation, resource -> {
                if (valid(resource)) {
                    List<ResourceAttribute> resourceAttributes = resourceAttributes(resource);
                    visible.set(resourceAttributes.size());
                    total.set(resourceAttributes.size());
                    dl = descriptionList().css(halComponent(resourceView))
                            .orientation(breakpoints(
                                    sm, vertical,
                                    md, vertical,
                                    lg, horizontal,
                                    xl, horizontal,
                                    _2xl, horizontal))
                            .horizontalTermWidth(breakpoints(
                                    lg, "23ch",
                                    xl, "25ch",
                                    _2xl, "28ch"));
                    for (ResourceAttribute ra : resourceAttributes) {
                        ResourceViewItem rvi = resourceViewItem(uic, metadata, ra);
                        dl.addItem(rvi.dlg);
                        updateFunctions.put(ra.name, rvi.update);
                        rvi.update.update(ra.value);
                    }
                    setVisible(toolbar, true);
                    viewContainer.append(dl.element());
                } else {
                    empty();
                }
            }, (op, error) -> resourceError(op.asCli(), error));
        } else {
            metadataError();
        }
    }

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
                        ModelNode nestedValue = ModelNodeHelper.nested(resource, nestedDescription.fullyQualifiedName());
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

    // ------------------------------------------------------ status

    private boolean valid(ModelNode resource) {
        return resource != null && resource.isDefined() && !resource.asPropertyList().isEmpty();
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

    private void metadataError() {
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

    private void resourceError(String operation, String error) {
        setVisible(toolbar, false);
        removeChildrenFrom(viewContainer);
        viewContainer.append(emptyState().size(Size.sm)
                .addHeader(emptyStateHeader()
                        .icon(exclamationCircle(), globalVar("danger-color", "100"))
                        .text("Operation failed"))
                .addBody(emptyStateBody()
                        .add("Unable to view resource. Operation ")
                        .add(code().textContent(operation))
                        .add(" failed")
                        .add(pre().textContent(error)))
                .element());
    }

    // ------------------------------------------------------ filter

    private void onFilterChanged(Filter<ResourceAttribute> filter, String origin) {
        if (isAttached(element())) {
            logger.debug("Filter attributes: %s", filter);
            int matchingItems;
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
                noMatch.toggle(viewContainer, matchingItems == 0);
            } else {
                matchingItems = total.get();
                noMatch.toggle(viewContainer, false);
                dl.items().forEach(dlg -> dlg.classList().remove(halModifier(filtered)));
            }
            visible.set(matchingItems);
        }
    }

    // ------------------------------------------------------ toolbar actions

    void refresh() {
        if (isAttached(element())) {
            uic.dispatcher().execute(operation, resource -> {
                if (valid(resource)) {
                    for (ResourceAttribute ra : resourceAttributes(resource)) {
                        UpdateValueFn updateAttribute = updateFunctions.get(ra.name);
                        if (updateAttribute != null) {
                            updateAttribute.update(ra.value);
                        } else {
                            logger.warn("Unable to update attribute %s. No update function found.", ra.name);
                        }
                    }
                } else {
                    empty();
                }
            }, (op, error) -> resourceError(op.asCli(), error));
        }
    }

    void resolve() {
        if (isAttached(element())) {

        }
    }

    void reset() {
        if (isAttached(element())) {

        }
    }

    void edit() {
        if (isAttached(element())) {

        }
    }
}
