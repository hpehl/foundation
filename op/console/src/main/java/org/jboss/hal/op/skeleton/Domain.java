package org.jboss.hal.op.skeleton;

import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.style.Size;

import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.icon.IconSets.fas.exclamationCircle;

public class Domain {

    public static EmptyState domainModeNotSupported(Size size) {
        return emptyState().size(size)
                .addHeader(emptyStateHeader()
                        .icon(exclamationCircle())
                        .text("Domain mode"))
                .addBody(emptyStateBody().textContent("Domain mode is not supported yet."));
    }
}
