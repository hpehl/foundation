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

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(ModelBrowser.class.getName());
    private final UIContext uic;
    private final HTMLElement root;
    private final ModelBrowserTree tree;
    private final ModelBrowserDetail detail;

    public ModelBrowser(UIContext uic) {
        this.uic = uic;
        this.tree = new ModelBrowserTree(uic);
        this.detail = new ModelBrowserDetail(uic);
        this.root = grid().span(12)
                .css(halComponent(modelBrowser))
                .addItem(gridItem().span(4).add(tree))
                .addItem(gridItem().span(8).add(detail))
                .element();
        tree.detail = detail;
        detail.tree = tree;
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ api

    public void show(AddressTemplate template) {
        if (template.fullyQualified()) {
            ResourceAddress address = template.resolve(uic.statementContext);
            Operation operation = new Operation.Builder(address, READ_CHILDREN_TYPES_OPERATION)
                    .param(INCLUDE_SINGLETONS, true)
                    .build();
            uic.dispatcher.execute(operation, result -> {
                tree.show(parseChildren(template, operation.getName(), result));
                detail.show(new ModelBrowserNode(template, "Management Model", RESOURCE));
            });
        } else {
            logger.error("Illegal address: %s. Please specify a fully qualified address not ending with '*'", template);
        }
    }
}
