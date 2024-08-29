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
package org.jboss.hal.meta.filter;

import java.util.List;
import java.util.function.Function;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;

public class TypesFilterValue<T> extends FilterValue<T, List<ModelType>> {

    public static final String NAME = "types";

    public TypesFilterValue(Function<T, ModelNode> modelNodeFn) {
        super(NAME, emptyList(), true, (object, types) -> {
            if (!types.isEmpty()) {
                ModelType attributeType = modelNodeFn.apply(object).get(TYPE).asType();
                return types.contains(attributeType);
            }
            return true;
        });
    }

    @Override
    protected String save() {
        return value.stream().map(modelType -> String.valueOf(modelType.getTypeChar())).collect(joining());
    }

    @Override
    protected void load(String value) {
        if (value != null && !value.isEmpty()) {
            List<ModelType> modelTypes = value.chars().mapToObj(c -> ModelType.forChar((char) c)).collect(toList());
            set(modelTypes);
        }
    }
}
