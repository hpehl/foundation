package org.jboss.hal.meta.description;

import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.env.Stability;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPRECATED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STABILITY;

interface Description {

    ModelNode modelNode();

    default String description() {
        return modelNode().get(DESCRIPTION).asString();
    }

    default Stability stability() {
        return ModelNodeHelper.asEnumValue(modelNode(), STABILITY, Stability::valueOf, Stability.DEFAULT);
    }

    default Deprecation deprecation() {
        if (modelNode().hasDefined(DEPRECATED)) {
            return new Deprecation(modelNode().get(DEPRECATED));
        }
        return null;
    }

}
