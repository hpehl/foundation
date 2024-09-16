package org.jboss.hal.ui.filter;

import java.util.List;

public class AccessTypeValue {

    public static List<AccessTypeValue> accessTypeValues() {
        return List.of(new AccessTypeValue(AccessTypeFilterAttribute.NAME, "Read-write", "read-write"),
                new AccessTypeValue(AccessTypeFilterAttribute.NAME, "Read-only", "read-only"),
                new AccessTypeValue(AccessTypeFilterAttribute.NAME, "Metric", "-metric"));
    }

    public final String identifier;
    public final String text;
    public final String value;

    public AccessTypeValue(String filterAttribute, String text, String value) {
        this.identifier = filterAttribute + "-" + value;
        this.text = text;
        this.value = value;
    }
}
