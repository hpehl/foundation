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
import java.util.EnumSet;
import java.util.List;

import org.gwtproject.event.shared.HandlerRegistration;
import org.jboss.elemento.Attachable;
import org.jboss.elemento.EventType;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.Key;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.UIContext;
import org.jboss.hal.ui.modelbrowser.ModelBrowserSelectEvent;
import org.patternfly.component.button.Button;
import org.patternfly.popper.Modifiers;
import org.patternfly.popper.Popper;
import org.patternfly.popper.PopperBuilder;
import org.patternfly.popper.TriggerAction;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;
import elemental2.dom.Node;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.document;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.isVisible;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.bind;
import static org.jboss.hal.resources.HalClasses.capabilityReference;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.providedBy;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.list.List.list;
import static org.patternfly.component.list.ListItem.listItem;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.Gap.sm;
import static org.patternfly.layout.stack.Stack.stack;
import static org.patternfly.layout.stack.StackItem.stackItem;
import static org.patternfly.popper.Placement.auto;

class CapabilityReference implements IsElement<HTMLElement>, Attachable {

    // ------------------------------------------------------ factory

    static CapabilityReference capabilityReference(UIContext uic, String attribute, String capability) {
        return new CapabilityReference(uic, attribute, capability);
    }

    // ------------------------------------------------------ instance

    private enum State {
        VOID, NO_RESOURCES, ONE_RESOURCE, MULTIPLE_RESOURCES, FAILED, NO_VALUE
    }

    private static final int DISTANCE = 10;
    private static final int Z_INDEX = 9999;
    private static final Logger logger = Logger.getLogger(CapabilityReference.class.getName());

    private final UIContext uic;
    private final String attribute;
    private final String capability;
    private final List<HandlerRegistration> handlerRegistrations;
    private final HTMLElement valueElement;
    private final Button providedByButton;
    private final HTMLElement menuElement;
    private final HTMLElement menuCountElement;
    private final org.patternfly.component.list.List menuList;
    private final HTMLElement root;
    private State state;
    private String value;
    private AddressTemplate template;
    private Popper popper;

    CapabilityReference(UIContext uic, String attribute, String capability) {
        this.uic = uic;
        this.attribute = attribute;
        this.capability = capability;
        this.handlerRegistrations = new ArrayList<>();
        this.state = State.VOID;
        this.template = null;

        this.root = flex().css(halComponent(capabilityReference))
                .alignItems(center).columnGap(sm)
                .add(valueElement = span().css(halComponent(capabilityReference, Classes.value)).element())
                .add(small().css(halComponent(capabilityReference, providedBy))
                        .add(providedByButton = button("").link().inline().onClick((e, btn) -> onClick())))
                .element();

        this.menuElement = div()
                .add(stack().css(halComponent(capabilityReference, Classes.menu))
                        .gutter()
                        .addItem(stackItem()
                                .add("Capability ")
                                .add(code().textContent(capability))
                                .add(" is provided by ")
                                .add(menuCountElement = span().textContent("").element())
                                .add(" resources:"))
                        .addItem(stackItem().fill()
                                .add(menuList = list().css(halComponent(capabilityReference, Classes.menu, Classes.list))
                                        .plain())))
                .element();

        setVisible(menuElement, false);
        setVisible(providedByButton, false);
        body().add(menuElement);
        Attachable.register(this, this);
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        popper = new PopperBuilder("CapabilityReference", providedByButton.element(), menuElement)
                .zIndex(Z_INDEX)
                .placement(auto)
                .addModifier(Modifiers.offset(DISTANCE),
                        Modifiers.noOverflow(),
                        Modifiers.hide(),
                        Modifiers.flip(true),
                        Modifiers.widths(),
                        Modifiers.placement(),
                        Modifiers.eventListeners(false))
                .registerHandler(EnumSet.of(TriggerAction.manual), null, null)
                .removePopperOnTriggerDetach()
                .build();

        handlerRegistrations.add(bind(document, EventType.click, true, e -> {
            if (isVisible(menuElement)) {
                Node target = (Node) e.target;
                if (target != providedByButton.element() && !menuElement.contains(target)) {
                    popper.hide(null);
                }
            }
        }));
        handlerRegistrations.add(bind(document, EventType.keydown, true, e -> {
            if (isVisible(menuElement) && Key.Escape.match(e)) {
                popper.hide(null);
            }
        }));
    }

    @Override
    public void detach(MutationRecord mutationRecord) {
        for (HandlerRegistration handlerRegistration : handlerRegistrations) {
            handlerRegistration.removeHandler();
        }
        if (popper != null) {
            popper.cleanup();
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ api

    void updateValue(String value) {
        this.value = value;
        this.valueElement.textContent = value;

        menuList.clear();
        setVisible(providedByButton, false);
        findResources().then(__ -> {
            setVisible(providedByButton, state == State.ONE_RESOURCE || state == State.MULTIPLE_RESOURCES);
            return null;
        });
    }

    // ------------------------------------------------------ event

    private void onClick() {
        if (state == State.ONE_RESOURCE && template != null) {
            ModelBrowserSelectEvent.dispatch(element(), template);
        } else if (state == State.MULTIPLE_RESOURCES) {
            popper.show(null);
        } else {
            logger.error("Invalid state %s in capability reference with attribute %s, capability % and value %s",
                    state.name(), attribute, capability, value);
        }
    }

    // ------------------------------------------------------ internal

    // Only this method may change the state!
    private Promise<Void> findResources() {
        if (value != null && !value.isEmpty()) {
            return uic.capabilityRegistry().findResources(capability, value)
                    .then(templates -> {

                        if (templates.isEmpty()) {
                            state = State.NO_RESOURCES;
                            setVisible(providedByButton.element(), false);
                            logger.warn("No resources found for attribute %s, capability % and value %s",
                                    attribute, capability, value);

                        } else if (templates.size() == 1) {
                            state = State.ONE_RESOURCE;
                            template = templates.get(0);
                            providedByButton.text("provided by 1 resource");

                        } else {
                            state = State.MULTIPLE_RESOURCES;
                            String size = String.valueOf(templates.size());
                            List<AddressTemplate> sortedTemplates = templates.stream()
                                    .sorted(comparing(AddressTemplate::toString))
                                    .collect(toList());

                            menuCountElement.textContent = size;
                            menuList.addItems(sortedTemplates, tpl -> listItem()
                                    .add(button(tpl.toString()).link().inline()
                                            .onClick((e, btn) -> ModelBrowserSelectEvent.dispatch(element(), tpl))));
                            providedByButton.text("provided by " + size + " resources");
                        }
                        return Promise.resolve((Void) null);
                    })
                    .catch_(error -> {
                        state = State.FAILED;
                        logger.error("Unable to find resources for attribute %s, capability % and value %s: %s",
                                attribute, capability, value, String.valueOf(error));
                        return Promise.resolve((Void) null);
                    });
        } else {
            state = State.NO_VALUE;
            logger.error("Unable to find resources for attribute %s and capability %s: No value has been assigned!",
                    attribute, capability);
            return Promise.resolve((Void) null);
        }
    }
}
