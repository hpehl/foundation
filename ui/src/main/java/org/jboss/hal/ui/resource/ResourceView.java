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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.By;
import org.jboss.elemento.HasElement;
import org.jboss.elemento.Id;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.model.filter.Filter;
import org.jboss.hal.model.filter.FilterAttribute;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.LabelBuilder;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.codeblock.CodeBlock;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.label.Label;
import org.patternfly.component.list.DescriptionList;
import org.patternfly.component.list.DescriptionListTerm;
import org.patternfly.component.menu.SingleSelect;
import org.patternfly.component.switch_.Switch;
import org.patternfly.component.textinputgroup.TextInputGroup;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.core.Tuple;
import org.patternfly.icon.IconSets.fas;
import org.patternfly.style.Size;
import org.patternfly.style.Variables;

import elemental2.dom.HTMLElement;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.findAll;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORAGE;
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
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MenuToggle.menuToggle;
import static org.patternfly.component.menu.SingleSelect.singleSelect;
import static org.patternfly.component.menu.SingleSelectMenu.singleSelectMenu;
import static org.patternfly.component.popover.Popover.popover;
import static org.patternfly.component.popover.PopoverBody.popoverBody;
import static org.patternfly.component.textinputgroup.TextInputGroup.searchInputGroup;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarGroup.toolbarGroup;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.core.Tuple.tuple;
import static org.patternfly.icon.IconSets.fas.ban;
import static org.patternfly.icon.IconSets.fas.edit;
import static org.patternfly.icon.IconSets.fas.exclamationCircle;
import static org.patternfly.icon.IconSets.fas.link;
import static org.patternfly.icon.IconSets.fas.search;
import static org.patternfly.icon.IconSets.fas.undo;
import static org.patternfly.popper.Placement.auto;
import static org.patternfly.style.Breakpoint._2xl;
import static org.patternfly.style.Breakpoint.default_;
import static org.patternfly.style.Breakpoint.lg;
import static org.patternfly.style.Breakpoint.md;
import static org.patternfly.style.Breakpoint.sm;
import static org.patternfly.style.Breakpoint.xl;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Color.grey;
import static org.patternfly.style.Variable.globalVar;
import static org.patternfly.style.Variable.utilVar;

// TODO Implement toolbar with filters/flags:
//  Show/hide undefined
//  Show/hides default values
//  Show runtime/configuration
//  Resolve all expressions
//  Add reset/edit actions
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
    private final UIContext uic;
    private final Metadata metadata;
    private final LabelBuilder labelBuilder;
    private final ResourceFilter filter;
    private final List<String> attributes;
    private final Map<String, UpdateValueFn> updateValueFunctions;
    private final Toolbar toolbar;
    private final TextInputGroup filterByName;
    private final SingleSelect filterByDefined;
    private final SingleSelect filterByStorage;
    private final SingleSelect filterByAccessType;
    private final HTMLElement viewContainer;
    private final HTMLElement root;
    private DescriptionList dl;
    private EmptyState noAttributes;
    private boolean shown;

    ResourceView(UIContext uic, Metadata metadata) {
        this.uic = uic;
        this.metadata = metadata;
        this.labelBuilder = new LabelBuilder();
        this.filter = new ResourceFilter();
        this.attributes = new ArrayList<>();
        this.updateValueFunctions = new HashMap<>();
        this.shown = false;

        String resetId = Id.unique("reset");
        this.root = div()
                .add(toolbar = toolbar()
                        .addContent(toolbarContent()
                                .addItem(toolbarItem().css(modifier("search-filter"))
                                        .add(filterByName = searchInputGroup("Filter by name")
                                                .onChange((event, textInputGroup, value) -> {
                                                    filter.set(ResourceFilter.NAME, value);
                                                    filter();
                                                })))
                                .addGroup(toolbarGroup().css(modifier("filter-group"))
                                        .addItem(toolbarItem()
                                                .add(filterByDefined = singleSelect(menuToggle()
                                                        .icon(fas.filter())
                                                        .text("Defined"))
                                                        .addMenu(singleSelectMenu()
                                                                .onSingleSelect((event, menuItem, selected) -> {
                                                                    if ("all".equals(menuItem.identifier())) {
                                                                        filter.reset(ResourceFilter.UNDEFINED);
                                                                    } else {
                                                                        filter.set(ResourceFilter.UNDEFINED,
                                                                                menuItem.identifier());
                                                                    }
                                                                    filter();
                                                                })
                                                                .addContent(menuContent()
                                                                        .addList(menuList()
                                                                                .addItem("all", "All defined")
                                                                                .addItem("false", "Defined")
                                                                                .addItem("true", "Undefined"))))))
                                        .addItem(toolbarItem()
                                                .add(filterByStorage = singleSelect(menuToggle()
                                                        .icon(fas.filter())
                                                        .text("Storage"))
                                                        .addMenu(singleSelectMenu()
                                                                .onSingleSelect((event, menuItem, selected) -> {
                                                                    if ("all".equals(menuItem.identifier())) {
                                                                        filter.reset(ResourceFilter.STORAGE);
                                                                    } else {
                                                                        filter.set(ResourceFilter.STORAGE,
                                                                                menuItem.identifier());
                                                                    }
                                                                    filter();
                                                                })
                                                                .addContent(menuContent()
                                                                        .addList(menuList()
                                                                                .addItem("all", "All storage")
                                                                                .addItem("configuration", "Configuration")
                                                                                .addItem("runtime", "Runtime"))))))
                                        .addItem(toolbarItem()
                                                .add(filterByAccessType = singleSelect(menuToggle()
                                                        .icon(fas.filter())
                                                        .text("Access type"))
                                                        .addMenu(singleSelectMenu()
                                                                .onSingleSelect((event, menuItem, selected) -> {
                                                                    if ("all".equals(menuItem.identifier())) {
                                                                        filter.reset(ResourceFilter.ACCESS_TYPE);
                                                                    } else {
                                                                        filter.set(ResourceFilter.ACCESS_TYPE,
                                                                                menuItem.identifier());
                                                                    }
                                                                    filter();
                                                                })
                                                                .addContent(menuContent()
                                                                        .addList(menuList()
                                                                                .addItem("all", "All access type")
                                                                                .addItem("read-write", "Read-write")
                                                                                .addItem("read-only", "Read-only")
                                                                                .addItem("metric", "Metric")))))))
                                .addGroup(toolbarGroup().css(modifier("icon-button-group"), modifier("align-right"))
                                        .addItem(toolbarItem()
                                                .add(button().link().iconAndText(edit(), "Edit")))
                                        .addItem(toolbarItem()
                                                .add(button().id(resetId).link().iconAndText(undo(), "Reset"))
                                                .add(tooltip(By.id(resetId),
                                                        "Resets attributes to their initial or default value. Applied only to nillable attributes without relationships to other attributes.")
                                                        .placement(auto))))))
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
                for (ResourceAttribute ra : resourceAttributes(resource)) {
                    String label = labelBuilder.label(ra.name);
                    DescriptionListTerm term = label(ra, label);
                    Tuple<HTMLElement, UpdateValueFn> tuple = value(ra);
                    dl.addItem(descriptionListGroup(Id.build(ra.name, "group"))
                            .data(Filter.DATA, "")
                            .data(ResourceFilter.NAME, label.toLowerCase())
                            .data(ResourceFilter.UNDEFINED, String.valueOf(!ra.value.isDefined()))
                            .data(ResourceFilter.STORAGE, ra.description.get(STORAGE).asString())
                            .data(ResourceFilter.ACCESS_TYPE, ra.description.get(ACCESS_TYPE).asString())
                            .addTerm(term)
                            .addDescription(descriptionListDescription()
                                    .add(tuple.key)));
                    updateValueFunctions.put(ra.name, tuple.value);
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
                for (ResourceAttribute ra : resourceAttributes(resource)) {
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

    // ------------------------------------------------------ internal

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
                        .textContent("This resource has no attributes."))
                .element());
    }

    private void noAttributes() {
        noAttributes = emptyState().size(Size.sm)
                .addHeader(emptyStateHeader()
                        .icon(search())
                        .text("No results found"))
                .addBody(emptyStateBody()
                        .textContent("No results match the filter criteria. Clear all filters and try again."))
                .addFooter(emptyStateFooter()
                        .addActions(emptyStateActions()
                                .add(button("Clear all filters").link()
                                        .onClick((event, component) -> clearFilter()))));
        viewContainer.append(noAttributes.element());
    }

    private List<ResourceAttribute> resourceAttributes(ModelNode resource) {
        List<ResourceAttribute> resourceAttributes = new ArrayList<>();
        if (attributes.isEmpty()) {
            // collect all properties (including nested, record-like properties)
            for (Property property : resource.asPropertyList()) {
                String name = property.getName();
                ModelNode value = property.getValue();
                AttributeDescription description = metadata.resourceDescription().attributes().get(name);
                resourceAttributes.add(new ResourceAttribute(name, value, description));
                if (description.simpleRecord()) {
                    List<Property> nestedTypes = description.get(VALUE_TYPE).asPropertyList();
                    for (Property nestedType : nestedTypes) {
                        String nestedName = name + "." + nestedType.getName();
                        ModelNode nestedValue = ModelNodeHelper.nested(resource, nestedName);
                        AttributeDescription nestedDescription = new AttributeDescription(nestedType);
                        resourceAttributes.add(new ResourceAttribute(nestedName, nestedValue, nestedDescription));
                    }
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

    private DescriptionListTerm label(ResourceAttribute ra, String label) {
        DescriptionListTerm term = descriptionListTerm(label);
        if (ra.description != null) {
            if (uic.environment().highlightStability(metadata.resourceDescription().stability(), ra.description.stability())) {
                // Kind of a hack: Because DescriptionListTerm implements ElementDelegate
                // and delegates to the internal text element, we must use
                // term.element.appendChild() instead of term.add() to add the
                // stability label after the text element instead of into the text element.
                // Then we must reset the font weight to normal (DescriptionListTerm uses bold)
                term.style("align-items", "center");
                term.element().appendChild(stabilityLabel(ra.description.stability()).compact()
                        .style("align-self", "baseline")
                        .css(util("ml-sm"), util("font-weight-normal"))
                        .element());
            }
            if (ra.description.deprecation().isDefined()) {
                term.delegate().classList.add(halModifier(deprecated));
            }
            term.help(popover()
                    .css(util("min-width"))
                    .style(utilVar("min-width", Variables.MinWidth).name, "40ch")
                    .addHeader(label)
                    .addBody(popoverBody()
                            .add(attributeDescription(ra.description))));
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
                                element = span().element();
                                fn = value -> element.textContent = value.asString();
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

        fn.update(ra.value);
        return tuple(element, fn);
    }

    private void filter() {
        logger.debug("Filter for %s", filter);
        if (filter.isDefined()) {
            findAll(viewContainer, By.data(Filter.DATA)).forEach(e -> {
                boolean visible = true;
                for (Iterator<FilterAttribute> iterator = filter.iterator(); iterator.hasNext() && visible; ) {
                    FilterAttribute filterAttribute = iterator.next();
                    if (filterAttribute.isDefined()) {
                        String attributeValue = e.dataset.get(filterAttribute.name);
                        visible = filterAttribute.matches(attributeValue);
                    }
                }
                e.classList.toggle(halModifier(filtered), !visible);
            });
            if (dl != null) {
                int attributes = dl.items().size();
                int filteredAttributes = viewContainer.querySelectorAll(By.classname(halModifier(filtered)).selector()).length;
                if (attributes == filteredAttributes) {
                    noAttributes();
                }
            }
        }
    }

    private void clearFilter() {
        filter.resetAll();
        filterByName.main().value("", false);
        filterByDefined.menuToggle().text("All defined");
        filterByStorage.menuToggle().text("All storage");
        filterByAccessType.menuToggle().text("All access type");
        filterByDefined.menu().select("all", true, false);
        filterByStorage.menu().select("all", true, false);
        filterByAccessType.menu().select("all", true, false);
        failSafeRemoveFromParent(noAttributes);
        findAll(viewContainer, By.data(Filter.DATA)).forEach(e -> e.classList.remove(halModifier(filtered)));
    }
}
