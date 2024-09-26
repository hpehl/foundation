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

import org.jboss.elemento.IsElement;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.UIContext;

import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.modelBrowser;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserEngine.parseChildren;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.RESOURCE;
import static org.patternfly.layout.grid.Grid.grid;
import static org.patternfly.layout.grid.GridItem.gridItem;

public class ModelBrowser implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static ModelBrowser modelBrowser(UIContext uic) {
        return new ModelBrowser(uic);
    }

    /**
     * Creates and returns a custom event to select a resource for the provided address template in the model browser. The event
     * can only be used by elements which are part of the model browser DOM.
     * <p>
     * The model browser listens for these events and calls {@link #select(AddressTemplate)}. If the template is empty (equals
     * {@link AddressTemplate#root()}), {@link #home()} is called.
     *
     * @param source   the source element used to dispatch the event.
     * @param template the address template used to create the event's detail.
     * @see <a
     * href="https://developer.mozilla.org/en-US/docs/Web/Events/Creating_and_triggering_events">https://developer.mozilla.org/en-US/docs/Web/Events/Creating_and_triggering_events</a>
     */
    public static void dispatchSelectEvent(HTMLElement source, AddressTemplate template) {
        //noinspection unchecked
        CustomEventInit<String> init = CustomEventInit.create();
        init.setBubbles(true);
        init.setCancelable(true);
        init.setDetail(template == null ? "" : template.toString());
        CustomEvent<String> event = new CustomEvent<>(SELECT_TEMPLATE_EVENT, init);
        source.dispatchEvent(event);
    }

    // ------------------------------------------------------ instance

    public static final String SELECT_TEMPLATE_EVENT = "select-template";
    private static final Logger logger = Logger.getLogger(ModelBrowser.class.getName());
    private static final int TREE_COLUMNS = 3;
    private static final int DETAIL_COLUMNS = 9;
    private final UIContext uic;
    private final HTMLElement root;
    private final ModelBrowserTree tree;
    private final ModelBrowserDetail detail;
    private ModelBrowserNode rootMbn;

    public ModelBrowser(UIContext uic) {
        this.uic = uic;
        this.tree = new ModelBrowserTree(uic);
        this.detail = new ModelBrowserDetail(uic);
        this.root = grid().span(12)
                .css(halComponent(modelBrowser))
                .addItem(gridItem().span(TREE_COLUMNS).add(tree))
                .addItem(gridItem().span(DETAIL_COLUMNS).add(detail))
                .element();
        tree.detail = detail;
        detail.tree = tree;

        element().addEventListener(SELECT_TEMPLATE_EVENT, evt -> {
            //noinspection unchecked
            CustomEvent<String> customEvent = (CustomEvent<String>) evt;
            AddressTemplate template = AddressTemplate.of(customEvent.detail);
            if (template.isEmpty()) {
                home();
            } else {
                select(template);
            }
        });
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ api

    public void show(AddressTemplate template) {
        if (template.fullyQualified()) {
            uic.metadataRepository().lookup(template, metadata -> {
                ResourceAddress address = template.resolve(uic.statementContext());
                Operation operation = new Operation.Builder(address, READ_CHILDREN_TYPES_OPERATION)
                        .param(INCLUDE_SINGLETONS, true)
                        .build();
                uic.dispatcher().execute(operation, result -> {
                    String name = template.isEmpty() ? "Management Model" : template.last().value;
                    rootMbn = new ModelBrowserNode(template, name, RESOURCE);
                    tree.show(parseChildren(rootMbn, result, true));
                    detail.show(rootMbn);
                });
            });
        } else {
            logger.error("Illegal address: %s. Please specify a fully qualified address not ending with '*'", template);
        }
    }

    public void home() {
        if (rootMbn != null) {
            tree.unselect();
            detail.show(rootMbn);
        }
    }

    public void select(AddressTemplate template) {
        if (rootMbn != null) {
            if (template.template.startsWith(rootMbn.template.template)) {
                tree.select(template);
            } else {
                logger.error("Unable to select %s: %s is not a sub-template of %s",
                        template, template, rootMbn.template);
            }
        } else {
            logger.error("Unable to select %s: Root template is null!", template);
        }
    }
}
