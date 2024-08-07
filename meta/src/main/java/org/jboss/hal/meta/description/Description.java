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
package org.jboss.hal.meta.description;

import java.util.Random;

import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.env.Stability;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPRECATED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;

public interface Description {

    ModelNode modelNode();

    default String description() {
        return modelNode().get(DESCRIPTION).asString();
    }

    default Stability stability() {
        int index = new Random().nextInt(Stability.values().length);
        return Stability.values()[index];
        // return ModelNodeHelper.asEnumValue(modelNode(), STABILITY, Stability::valueOf, Stability.DEFAULT);
    }

    default Deprecation deprecation() {
        if (modelNode().hasDefined(DEPRECATED)) {
            return new Deprecation(modelNode().get(DEPRECATED));
        }
        return null;
    }

}
