package org.jboss.hal.ui.filter;

import java.util.List;

public class StorageValue {

    public static List<StorageValue> storageValues() {
        return List.of(new StorageValue(StorageFilterAttribute.NAME, "Configuration", "configuration"),
                new StorageValue(StorageFilterAttribute.NAME, "Runtime", "runtime"));
    }

    public final String identifier;
    public final String text;
    public final String value;

    public StorageValue(String filterAttribute, String text, String value) {
        this.identifier = filterAttribute + "-" + value;
        this.text = text;
        this.value = value;
    }
}
