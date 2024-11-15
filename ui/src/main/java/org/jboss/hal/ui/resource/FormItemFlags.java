package org.jboss.hal.ui.resource;

class FormItemFlags {

    enum Placeholder {
        NONE,
        UNDEFINED,
        DEFAULT_VALUE
    }

    final Placeholder placeholder;

    FormItemFlags(Placeholder placeholder) {
        this.placeholder = placeholder;
    }
}
