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
package org.jboss.hal.dmr;

import java.util.function.Function;

import org.jboss.hal.env.Version;

import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_MAJOR_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_MICRO_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_MINOR_VERSION;

public final class ModelNodeHelper {

    // ------------------------------------------------------ enums

    /**
     * Looks for the specified attribute and tries to convert it to an enum constant using
     * {@code LOWER_HYPHEN.to(UPPER_UNDERSCORE, modelNode.get(attribute).asString())}.
     */
    public static <E extends Enum<E>> E asEnumValue(ModelNode modelNode, String attribute, Function<String, E> valueOf,
            E defaultValue) {
        if (modelNode.hasDefined(attribute)) {
            return asEnumValue(modelNode.get(attribute), valueOf, defaultValue);
        }
        return defaultValue;
    }

    public static <E extends Enum<E>> E asEnumValue(ModelNode modelNodeValue, Function<String, E> valueOf, E defaultValue) {
        E value = defaultValue;
        String convertedValue = modelNodeValue.asString().replace('-', '_').toUpperCase();
        try {
            value = valueOf.apply(convertedValue);
        } catch (IllegalArgumentException ignored) {
        }
        return value;
    }

    // ------------------------------------------------------ version

    public static Version parseVersion(ModelNode modelNode) {
        if (modelNode.hasDefined(MANAGEMENT_MAJOR_VERSION) &&
                modelNode.hasDefined(MANAGEMENT_MINOR_VERSION) &&
                modelNode.hasDefined(MANAGEMENT_MICRO_VERSION)) {
            int major = modelNode.get(MANAGEMENT_MAJOR_VERSION).asInt();
            int minor = modelNode.get(MANAGEMENT_MINOR_VERSION).asInt();
            int micro = modelNode.get(MANAGEMENT_MICRO_VERSION).asInt();
            return new Version(major, minor, micro);
        }
        return Version.EMPTY_VERSION;
    }

    // ------------------------------------------------------  instance

    private ModelNodeHelper() {}
}
