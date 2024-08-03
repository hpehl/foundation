package org.jboss.hal.ui.modelbrowser;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

class Types {

    static String formatType(ModelNode hasType) {
        StringBuilder builder = new StringBuilder();
        if (hasType.hasDefined(TYPE)) {
            builder.append(hasType.get(TYPE).asString());
            if (hasType.hasDefined(VALUE_TYPE)) {
                ModelNode node = hasType.get(VALUE_TYPE);
                if (ModelType.TYPE.equals(node.getType())) {
                    builder.append("<").append(node.asString()).append(">");
                }
            }
        } else {
            builder.append(Names.NOT_AVAILABLE);
        }
        return builder.toString();
    }
}
