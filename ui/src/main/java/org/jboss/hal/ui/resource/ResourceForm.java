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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.elemento.HasElement;
import org.patternfly.component.HasItems;
import org.patternfly.component.form.Form;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.resources.HalClasses.edit;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resourceManager;
import static org.patternfly.component.form.Form.form;

class ResourceForm implements
        HasElement<HTMLElement, ResourceForm>,
        HasItems<HTMLElement, ResourceForm, FormItem> {

    private final Map<String, FormItem> items;
    private final Form form;

    ResourceForm() {
        this.items = new LinkedHashMap<>();
        this.form = form().css(halComponent(resourceManager, edit))
                .horizontal();
    }

    @Override
    public HTMLElement element() {
        return form.element();
    }

    @Override
    public ResourceForm that() {
        return this;
    }

    @Override
    public ResourceForm add(FormItem item) {
        items.put(item.identifier(), item);
        form.addItem(item.formGroup);
        return this;
    }

    @Override
    public Iterator<FormItem> iterator() {
        return items.values().iterator();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean contains(String identifier) {
        return items.containsKey(identifier);
    }

    @Override
    public void clear() {
        form.clear();
        items.clear();
    }

    boolean valid() {
        // TODO Validate form items
        return true;
    }
}
