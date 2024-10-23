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
package org.jboss.hal.ui.resource;

import java.util.HashMap;
import java.util.Map;

import org.jboss.elemento.HasElement;
import org.jboss.elemento.logger.Logger;
import org.patternfly.component.WithIdentifier;
import org.patternfly.component.form.FormGroup;
import org.patternfly.core.ComponentContext;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;

/** An item for a {@link ResourceForm} based on a {@link FormGroup} */
abstract class FormItem implements
        ManagerItem<FormItem>,
        HasElement<HTMLElement, FormItem>,
        ComponentContext<HTMLElement, FormItem>,
        WithIdentifier<HTMLElement, FormItem> {

    private static final Logger logger = Logger.getLogger(FormItem.class.getName());

    FormGroup formGroup;
    private final String identifier;
    private final Map<String, Object> data;

    FormItem(String identifier) {
        this.identifier = identifier;
        this.data = new HashMap<>();
    }

    @Override
    public HTMLElement element() {
        if (formGroup == null) {
            logger.error("Element for form item %s has not been initialized!", identifier);
            return span().element();
        }
        return formGroup.element();
    }

    @Override
    public FormItem that() {
        return this;
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public <T> FormItem store(String key, T value) {
        data.put(key, value);
        return this;
    }

    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (data.containsKey(key)) {
            return (T) data.get(key);
        }
        return null;
    }
}
