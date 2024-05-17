package org.jboss.hal.op.bootstrap;

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;

import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.location;
import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.span;
import static org.patternfly.component.codeblock.CodeBlock.codeBlock;
import static org.patternfly.component.expandable.ExpandableSection.expandableSection;
import static org.patternfly.component.expandable.ExpandableSectionContent.expandableSectionContent;
import static org.patternfly.component.expandable.ExpandableSectionToggle.expandableSectionToggle;
import static org.patternfly.component.popover.Popover.popover;
import static org.patternfly.style.Classes.util;

class EndpointDescription implements IsElement<HTMLElement> {

    private final HTMLElement root;

    EndpointDescription() {
        String allowedOriginId = Id.unique();
        this.root = div()
                .add(p()
                        .add("HAL runs in standalone mode. To continue, you must connect to a management interface. This management interface must have an ")
                        .add(span().css("hal-inline-help")
                                .id(allowedOriginId)
                                .textContent("allowed origin"))
                        .add(" for ")
                        .add(span().textContent(location.origin))
                        .add("."))
                .add(popover()
                        .trigger(By.id(allowedOriginId))
                        .addHeader("Allowed origin")
                        .addBody("An allowed origin is a trusted origin for sending Cross-Origin Resource Sharing (CORS) requests on the management API once the user is authenticated."))
                .add(expandableSection()
                        .addToggle(expandableSectionToggle("Show more"))
                        .addContent(expandableSectionContent()
                                .add(p().css(util("mb-sm"))
                                        .add("Use the following CLI commands to add an allowed origin for ")
                                        .add(span().textContent(location.origin))
                                        .add(".")
                                        .add(br())
                                        .add(small()
                                                .add("For domain mode prepend the first command with ")
                                                .add(code().textContent("/host=primary"))
                                                .add(" and use ")
                                                .add(code().textContent("reload --host=primary"))
                                                .add(" to reload the server.")))
                                .add(codeBlock()
                                        .code("/core-service=management/management-interface=http-interface:list-add(name=allowed-origins,value=" + location.origin + ")\nreload"))))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
