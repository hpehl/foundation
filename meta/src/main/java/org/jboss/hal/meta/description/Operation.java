package org.jboss.hal.meta.description;

import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPRECATED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUEST_PROPERTIES;

public class Operation extends NamedNode {

    private final Operations operations;

    public Operation(Operations operations, Property property) {
        super(property);
        this.operations = operations;
    }

    public Operation(Operations operations, String name, ModelNode node) {
        super(name, node);
        this.operations = operations;
    }

    public Attributes requestProperties() {
        return new Attributes(get(REQUEST_PROPERTIES));
    }

    public boolean deprecated() {
        return hasDefined(DEPRECATED) && get(DEPRECATED).asBoolean();
    }

    public Deprecation deprecation() {
        if (deprecated()) {
            return new Deprecation(get(DEPRECATED));
        }
        return null;
    }
}
