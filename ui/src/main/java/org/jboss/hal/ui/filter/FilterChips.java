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

import java.util.ArrayList;
import java.util.List;

import org.patternfly.component.chip.Chip;
import org.patternfly.filter.Filter;

import static org.patternfly.component.chip.Chip.chip;
import static org.patternfly.filter.FilterAttributeModifier.collectionRemove;

public class FilterChips {

    public static <T> List<Chip> definedRequiredDeprecatedChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        chips.addAll(booleanChips(filter, DefinedAttribute.NAME, "Defined", "Undefined"));
        chips.addAll(booleanChips(filter, RequiredAttribute.NAME, "Required", "Optional"));
        chips.addAll(deprecatedChips(filter));
        return chips;
    }

    public static <T> List<Chip> requiredDeprecatedChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        chips.addAll(booleanChips(filter, RequiredAttribute.NAME, "Required", "Optional"));
        chips.addAll(deprecatedChips(filter));
        return chips;
    }

    public static <T> List<Chip> deprecatedChips(Filter<T> filter) {
        return booleanChips(filter, DeprecatedAttribute.NAME, "Deprecated", "Not deprecated");
    }

    public static <T> List<Chip> storageAccessTypeChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        chips.addAll(storageChips(filter));
        chips.addAll(accessTypeChips(filter));
        return chips;
    }

    public static <T> List<Chip> storageChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        if (filter.defined(StorageAttribute.NAME)) {
            StorageValue storageValue = filter.<StorageValue>get(StorageAttribute.NAME).value();
            chips.add(chip(storageValue.text)
                    .onClose((e, c) -> filter.reset(StorageAttribute.NAME)));
        }
        return chips;
    }

    public static <T> List<Chip> accessTypeChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        if (filter.defined(AccessTypeAttribute.NAME)) {
            AccessTypeValue accessTypeValue = filter.<AccessTypeValue>get(AccessTypeAttribute.NAME).value();
            chips.add(chip(accessTypeValue.text)
                    .onClose((e, c) -> filter.reset(AccessTypeAttribute.NAME)));
        }
        return chips;
    }

    public static <T> List<Chip> typeChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        if (filter.defined(TypesAttribute.NAME)) {
            List<TypeValues> value = filter.<List<TypeValues>>get(TypesAttribute.NAME).value();
            for (TypeValues type : value) {
                chips.add(chip(type.name).onClose((event, chip) ->
                        filter.set(TypesAttribute.NAME, List.of(type), collectionRemove(ArrayList::new))));
            }
        }
        return chips;
    }

    public static <T> List<Chip> parametersReturnValueChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        chips.addAll(booleanChips(filter, ParametersAttribute.NAME, "Parameters", "No parameters"));
        chips.addAll(booleanChips(filter, ReturnValueAttribute.NAME, "Return value", "No return value"));
        return chips;
    }

    // ------------------------------------------------------ internal

    private static <T> List<Chip> booleanChips(Filter<T> filter, String filterAttribute, String true_, String false_) {
        List<Chip> chips = new ArrayList<>();
        if (filter.defined(filterAttribute)) {
            Boolean value = filter.<Boolean>get(filterAttribute).value();
            chips.add(chip(value ? true_ : false_).onClose((e, c) -> filter.reset(filterAttribute)));
        }
        return chips;
    }
}
