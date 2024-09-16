package org.jboss.hal.ui.filter;

import java.util.ArrayList;
import java.util.List;

import org.patternfly.component.chip.Chip;
import org.patternfly.filter.Filter;

import static org.patternfly.component.chip.Chip.chip;
import static org.patternfly.filter.FilterAttributeModifier.collectionRemove;

public class FilterChips {

    public static <T> List<Chip> statusChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        chips.addAll(definedChips(filter));
        chips.addAll(deprecatedChips(filter));
        return chips;
    }

    public static <T> List<Chip> definedChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        if (filter.defined(DefinedFilterAttribute.NAME)) {
            Boolean value = filter.<Boolean>get(DefinedFilterAttribute.NAME).value();
            chips.add(chip(value ? "Defined" : "Undefined")
                    .onClose((e, c) -> filter.reset(DefinedFilterAttribute.NAME)));
        }
        return chips;
    }

    public static <T> List<Chip> deprecatedChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        if (filter.defined(DeprecatedFilterAttribute.NAME)) {
            Boolean value = filter.<Boolean>get(DeprecatedFilterAttribute.NAME).value();
            chips.add(chip(value ? "Deprecated" : "Not deprecated")
                    .onClose((e, c) -> filter.reset(DeprecatedFilterAttribute.NAME)));
        }
        return chips;
    }

    public static <T> List<Chip> modeChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        chips.addAll(storageChips(filter));
        chips.addAll(accessTypeChips(filter));
        return chips;
    }

    public static <T> List<Chip> storageChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        if (filter.defined(StorageFilterAttribute.NAME)) {
            StorageValue storageValue = filter.<StorageValue>get(StorageFilterAttribute.NAME).value();
            chips.add(chip(storageValue.text)
                    .onClose((e, c) -> filter.reset(StorageFilterAttribute.NAME)));
        }
        return chips;
    }

    public static <T> List<Chip> accessTypeChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        if (filter.defined(AccessTypeFilterAttribute.NAME)) {
            AccessTypeValue accessTypeValue = filter.<AccessTypeValue>get(AccessTypeFilterAttribute.NAME).value();
            chips.add(chip(accessTypeValue.text)
                    .onClose((e, c) -> filter.reset(AccessTypeFilterAttribute.NAME)));
        }
        return chips;
    }

    public static <T> List<Chip> typeChips(Filter<T> filter) {
        List<Chip> chips = new ArrayList<>();
        if (filter.defined(TypesFilterAttribute.NAME)) {
            List<TypeValues> value = filter.<List<TypeValues>>get(TypesFilterAttribute.NAME).value();
            for (TypeValues type : value) {
                chips.add(chip(type.name).onClose((event, chip) ->
                        filter.set(TypesFilterAttribute.NAME, List.of(type), collectionRemove(ArrayList::new))));
            }
        }
        return chips;
    }
}
