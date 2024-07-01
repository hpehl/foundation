package org.jboss.hal.ui.form;

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
