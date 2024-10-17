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
package org.jboss.hal.op.dashboard;

import org.jboss.elemento.IsElement;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.style.Size;

import elemental2.dom.HTMLElement;

import static org.patternfly.style.Size.xs;

interface DashboardCard extends IsElement<HTMLElement> {

    void refresh();

    /**
     * @return an empty state of size {@link Size#sm} for usage in a dashboard card.
     */
    static EmptyState emptyState() {
        return EmptyState.emptyState().size(xs);
    }
}
