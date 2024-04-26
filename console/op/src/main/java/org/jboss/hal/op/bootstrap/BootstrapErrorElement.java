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
package org.jboss.hal.op.bootstrap;

import org.jboss.elemento.IsElement;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.icon.IconSets.fas.exclamationCircle;
import static org.patternfly.style.Size.lg;
import static org.patternfly.style.Variable.globalVar;

public class BootstrapErrorElement implements IsElement<HTMLElement> {

    private final HTMLElement root;

    public BootstrapErrorElement(BootstrapError error) {
        this.root = emptyState().size(lg)
                .addHeader(emptyStateHeader()
                        .icon(exclamationCircle(), globalVar("danger-color", "100"))
                        .text(header(error)))
                .addBody(emptyStateBody()
                        .textContent(text(error)))
                .element();
    }

    private String header(BootstrapError error) {
        switch (error.failure()) {
            case NETWORK_ERROR:
                return "Network Error";
            case NO_ENDPOINT_GIVEN:
                return "No endpoint given";
            case NO_ENDPOINT_FOUND:
                return "No endpoint found";
            case NO_LOCAL_STORAGE:
                return "No local storage";
            case NOT_AN_ENDPOINT:
                return "Not an endpoint";
            case ENVIRONMENT_ERROR:
                return "Environment error";
            case UNKNOWN:
                return "Unknown error";
        }
        return "Unknown error";
    }

    private String text(BootstrapError error) {
        switch (error.failure()) {
            case NETWORK_ERROR:
                return error.data() != null ? error.data() : "Unknown network error.";
            case NO_ENDPOINT_GIVEN:
                return "There was no endpoint given using the query parameter: " + error.data();
            case NO_ENDPOINT_FOUND:
                return "The endpoint " + error.data() + " was not found";
            case NO_LOCAL_STORAGE:
                return "The endpoint " + error.data() + " could not be read from the local storage";
            case NOT_AN_ENDPOINT:
                return "The endpoint" + error.data() + " is not reachable.";
            case ENVIRONMENT_ERROR:
                return "There was an error when reading the environment: " + error.data();
            case UNKNOWN:
                return error.data() != null ? error.data() : "Unknown error.";
        }
        return "Unknown error";
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
