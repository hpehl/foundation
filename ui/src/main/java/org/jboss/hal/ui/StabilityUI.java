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

import java.util.function.Supplier;

import org.jboss.hal.env.Stability;
import org.patternfly.icon.PredefinedIcon;
import org.patternfly.style.Color;

import static org.jboss.hal.env.Stability.EXPERIMENTAL;
import static org.jboss.hal.env.Stability.PREVIEW;
import static org.patternfly.icon.IconSets.fas.exclamationTriangle;
import static org.patternfly.icon.IconSets.fas.flask;
import static org.patternfly.icon.IconSets.fas.infoCircle;
import static org.patternfly.style.Color.blue;
import static org.patternfly.style.Color.gold;
import static org.patternfly.style.Color.red;

public class StabilityUI {

    public static Color color(Stability stability) {
        if (stability == EXPERIMENTAL) {
            return red;
        } else if (stability == PREVIEW) {
            return gold;
        }
        return blue;
    }

    public static PredefinedIcon icon(Stability stability) {
        if (stability == EXPERIMENTAL) {
            return flask();
        } else if (stability == PREVIEW) {
            return exclamationTriangle();
        }
        return infoCircle();
    }

    public static Supplier<PredefinedIcon> iconSupplier(Stability stability) {
        return () -> icon(stability);
    }
}
