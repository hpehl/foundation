package org.jboss.hal.ui.resource;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.gwtproject.event.shared.HandlerRegistration;
import org.jboss.elemento.Attachable;
import org.jboss.elemento.EventType;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.Key;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.ui.UIContext;
import org.jboss.hal.ui.modelbrowser.ModelBrowserSelectEvent;
import org.patternfly.component.SelectionMode;
import org.patternfly.component.button.Button;
import org.patternfly.component.menu.Menu;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.menu.MenuType;
import org.patternfly.popper.Modifiers;
import org.patternfly.popper.Popper;
import org.patternfly.popper.PopperBuilder;
import org.patternfly.popper.TriggerAction;

import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;

import static elemental2.dom.DomGlobal.clearTimeout;
import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.isVisible;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.bind;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.menu.Menu.menu;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.core.Timeouts.LOADING_TIMEOUT;
import static org.patternfly.popper.Placement.bottomStart;

class CapabilityReferenceLink implements IsElement<HTMLElement>, Attachable {

    private static final Logger logger = Logger.getLogger(CapabilityReferenceLink.class.getName());
    static final int DISTANCE = 10;
    static final int Z_INDEX = 9999;

    private final UIContext uic;
    private final String capability;
    private final List<HandlerRegistration> handlerRegistrations;
    private final Button button;
    private final Menu menu;
    private final MenuList menuList;
    private final HTMLElement root;
    private Popper popper;
    private String value;

    CapabilityReferenceLink(UIContext uic, String capability) {
        this.uic = uic;
        this.capability = capability;
        this.handlerRegistrations = new ArrayList<>();
        this.menu = menu(MenuType.menu, SelectionMode.click)
                .style("display", "none")
                .scrollable()
                .addContent(menuContent()
                        .addList(menuList = menuList()));
        this.button = button()
                .link()
                .inline()
                .progress(false, "Search for capability")
                .onClick((e, btn) -> {
                    if (isVisible(menu.element())) {
                        popper.hide(null);
                    } else {
                        findCapability(btn);
                    }
                });
        this.root = span()
                .add(tooltip(button.element(), "Follow capability references " + capability))
                .add(button)
                .element();

        body().add(menu);
        Attachable.register(this, this);
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        popper = new PopperBuilder("CapabilityReferenceLink", button.element(), menu.element())
                .zIndex(Z_INDEX)
                .placement(bottomStart)
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
            if (isVisible(menu.element())) {
                HTMLElement target = (HTMLElement) e.target;
                if (target != button.element() && !menu.element().contains(target)) {
                    popper.hide(null);
                }
            }
        }));
        handlerRegistrations.add(bind(document, EventType.keydown, true, e -> {
            if (isVisible(menu.element()) && Key.Escape.match(e)) {
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

    void assignValue(String value) {
        this.value = value;
        this.button.text(value);
    }

    private void findCapability(Button button) {
        if (value != null && !value.isEmpty()) {
            double handle = setTimeout(__ -> button.startProgress(), LOADING_TIMEOUT);
            uic.capabilityRegistry().findReference(capability, value)
                    .then(templates -> {
                        clearTimeout(handle);
                        button.stopProgress();
                        if (templates.isEmpty()) {
                            menuList.clear();
                            menuList.addItem(menuItem(Id.unique("crl", "not-found"), "No capabilities found!")
                                    .disabled(true));
                            popper.show(null);
                        } else if (templates.size() == 1) {
                            ModelBrowserSelectEvent.dispatch(button.element(), templates.get(0).toString());
                        } else {
                            menuList.clear();
                            menuList.addItems(templates, template -> menuItem(Id.unique("crl"), template.toString())
                                    .onClick((e, mi) -> ModelBrowserSelectEvent.dispatch(button.element(),
                                            template.toString())));
                            popper.show(null);
                        }
                        return null;
                    });
        } else {
            logger.error("Unable to find capability for %s. No value has been assigned!", capability);
        }
    }
}
