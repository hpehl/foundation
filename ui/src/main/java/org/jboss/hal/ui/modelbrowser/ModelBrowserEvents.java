package org.jboss.hal.ui.modelbrowser;

import java.util.function.Consumer;

import org.jboss.hal.event.UIEvent;
import org.jboss.hal.meta.AddressTemplate;

import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Umbrella interface for custom model browser UI events.
 * <p>
 * <strong>Please note</strong>
 * <br/> The events must only be triggered by elements which are part of the model browser DOM.
 */
public interface ModelBrowserEvents {

    // ------------------------------------------------------ add

    interface AddResource extends UIEvent {

        String TYPE = UIEvent.type("model-browser", "add");

        @JsType(namespace = JsPackage.GLOBAL, name = "Object", isNative = true)
        class Details {

            private static @JsOverlay Details create(AddressTemplate parent, String child, boolean singleton) {
                Details details = new Details();
                details.parent = parent.template;
                details.child = child;
                details.singleton = singleton;
                return details;
            }

            private String parent;
            private String child;
            private boolean singleton;

            @JsOverlay
            public final AddressTemplate parent() {
                return AddressTemplate.of(parent);
            }

            @JsOverlay
            public final String child() {
                return child;
            }

            @JsOverlay
            public final boolean singleton() {
                return singleton;
            }
        }

        /**
         * Creates and returns a custom event to add a singleton resource.
         *
         * @param source the source element used to dispatch the event.
         * @param parent the parent address used to create the event's detail.
         * @param child  the address used to create the event's detail.
         */
        static void dispatch(HTMLElement source, AddressTemplate parent, String child, boolean singleton) {
            //noinspection unchecked
            CustomEventInit<Details> init = CustomEventInit.create();
            init.setBubbles(true);
            init.setCancelable(true);
            init.setDetail(Details.create(parent, child, singleton));
            CustomEvent<Details> event = new CustomEvent<>(TYPE, init);
            source.dispatchEvent(event);
        }

        static void listen(HTMLElement element, Consumer<Details> listener) {
            element.addEventListener(TYPE, event -> {
                //noinspection unchecked
                CustomEvent<Details> customEvent = (CustomEvent<Details>) event;
                listener.accept(customEvent.detail);
            });
        }
    }

    // ------------------------------------------------------ select

    interface SelectInTree extends UIEvent {

        String TYPE = UIEvent.type("model-browser", "select-in-tree");

        /**
         * Creates and returns a custom event to select a resource in the model browser tree.
         *
         * @param source   the source element used to dispatch the event.
         * @param template the address used to create the event's detail.
         */
        static void dispatch(HTMLElement source, AddressTemplate template) {
            //noinspection unchecked
            CustomEventInit<String> init = CustomEventInit.create();
            init.setBubbles(true);
            init.setCancelable(true);
            init.setDetail(template.template);
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
}
