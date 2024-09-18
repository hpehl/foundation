package org.jboss.hal.ui.filter;

import org.jboss.elemento.IsElement;
import org.patternfly.core.ObservableValue;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.patternfly.style.Variable.globalVar;

public class ItemCount implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static ItemCount itemCount(ObservableValue<Integer> visible, ObservableValue<Integer> total, String singular,
            String plural) {
        return new ItemCount(visible, total, singular, plural);
    }

    // ------------------------------------------------------ instance

    private final String singular;
    private final String plural;
    private final HTMLElement root;

    ItemCount(ObservableValue<Integer> visible, ObservableValue<Integer> total, String singular, String plural) {
        this.singular = singular;
        this.plural = plural;
        this.root = span()
                .style("color", globalVar("Color", "200").asVar())
                .style("font-size", globalVar("FontSize", "sm").asVar())
                .element();
        visible.subscribe((v, __) -> root.textContent = text(v, total.get()));
        total.subscribe((t, __) -> root.textContent = text(visible.get(), t));
        visible.publish();
        total.publish();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    String text(int v, int t) {
        if (v == t) {
            return t + " " + (t == 1 ? singular : plural);
        } else {
            return v + " / " + t + " " + plural;
        }
    }
}
