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
package org.jboss.hal.ui.modelbrowser;

import java.util.function.Consumer;

import org.jboss.hal.event.UIEvent;
import org.jboss.hal.meta.AddressTemplate;

import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.HTMLElement;

public interface ModelBrowserSelectEvent extends UIEvent {

    String TYPE = "select-in-model-browser";

    /**
     * Creates and returns a custom event to select a resource of the provided address template in the model browser. The event
     * can only be used by elements which are part of the model browser DOM.
     *
     * @param source  the source element used to dispatch the event.
     * @param address the address used to create the event's detail.
     */
    static void dispatch(HTMLElement source, String address) {
        //noinspection unchecked
        CustomEventInit<String> init = CustomEventInit.create();
        init.setBubbles(true);
        init.setCancelable(true);
        init.setDetail(address);
        CustomEvent<String> event = new CustomEvent<>(TYPE, init);
        source.dispatchEvent(event);
    }

    static void listen(HTMLElement element, Consumer<AddressTemplate> listener) {
        element.addEventListener(TYPE, event -> {
            //noinspection unchecked
            CustomEvent<String> customEvent = (CustomEvent<String>) event;
            AddressTemplate template = AddressTemplate.of(customEvent.detail);
            listener.accept(template);
        });
    }
}
