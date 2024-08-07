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
package org.jboss.hal.ui;

import org.jboss.elemento.IsElement;
import org.jboss.hal.env.Stability;
import org.patternfly.component.label.Label;
import org.patternfly.core.Aria;
import org.patternfly.icon.PredefinedIcon;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.BuildingBlocks.stabilityColor;
import static org.jboss.hal.ui.BuildingBlocks.stabilityIcon;
import static org.patternfly.component.label.Label.label;

public class StabilityLabel implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static StabilityLabel stabilityLabel(Stability stability) {
        return new StabilityLabel(stability, true);
    }

    public static StabilityLabel stabilityLabel(Stability stability, boolean icon) {
        return new StabilityLabel(stability, icon);
    }

    // ------------------------------------------------------ instance

    private final Stability stability;
    private final Label label;

    StabilityLabel(Stability stability, boolean icon) {
        this.stability = stability;
        PredefinedIcon pi = stabilityIcon(stability);
        label = label(stability.label, stabilityColor(stability));
        if (icon) {
            label.icon(pi);
        }
    }

    @Override
    public HTMLElement element() {
        return label.element();
    }

    // ------------------------------------------------------ modifier

    public StabilityLabel compact() {
        label.compact()
                .removeIcon()
                .text(stability.letter)
                .aria(Aria.label, stability.label)
                .title(stability.label);
        return this;
    }
}
