package org.jboss.hal.ui.form;

import org.jboss.elemento.IsElement;
import org.jboss.hal.env.Stability;
import org.patternfly.component.label.Label;
import org.patternfly.core.Aria;
import org.patternfly.icon.PredefinedIcon;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.ui.form.StabilityUI.color;
import static org.jboss.hal.ui.form.StabilityUI.icon;
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

    public StabilityLabel(Stability stability, boolean icon) {
        this.stability = stability;
        PredefinedIcon pi = icon(stability);
        label = label(stability.label, color(stability));
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
