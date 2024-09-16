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
package org.jboss.hal.ui.filter;

import java.util.List;
import java.util.function.Function;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.patternfly.filter.FilterAttribute;

import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;

public class TypesFilterAttribute<T> extends FilterAttribute<T, List<ModelType>> {

    public static final String NAME = "types";

    public TypesFilterAttribute(Function<T, ModelNode> modelNodeFn) {
        super(NAME, (object, types) -> {
            ModelType attributeType = modelNodeFn.apply(object).get(TYPE).asType();
            return types.contains(attributeType);
        });
    }
}