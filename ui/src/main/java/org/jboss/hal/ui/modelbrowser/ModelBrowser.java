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

    public static ModelBrowser modelBrowser(UIContext uic, AddressTemplate template) {
        return new ModelBrowser(uic, template);
    }

    // ------------------------------------------------------ instance

    private static final Logger logger = Logger.getLogger(ModelBrowser.class.getName());
    private static final int TREE_COLUMNS = 3;
    private static final int DETAIL_COLUMNS = 9;
    final ModelBrowserTree tree;
    final ModelBrowserDetail detail;
    private final UIContext uic;
    private final AddressTemplate template;
    private final HTMLElement root;
    private ModelBrowserNode rootMbn;

    public ModelBrowser(UIContext uic, AddressTemplate template) {
        this.uic = uic;
        this.template = template;
        this.tree = new ModelBrowserTree(uic, this);
        this.detail = new ModelBrowserDetail(uic, this);
        this.root = grid().span(12)
                .css(halComponent(modelBrowser))
                .addItem(gridItem().span(TREE_COLUMNS).add(tree))
                .addItem(gridItem().span(DETAIL_COLUMNS).add(detail))
                .element();

        ModelBrowserSelectEvent.listen(root, this::select);
        load();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ api

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

    public void reload() {
        load();
    }

    // ------------------------------------------------------ internal

    private void load() {
        if (template.fullyQualified()) {
            uic.metadataRepository().lookup(template, metadata -> {
                ResourceAddress address = template.resolve(uic.statementContext());
                Operation operation = new Operation.Builder(address, READ_CHILDREN_TYPES_OPERATION)
                        .param(INCLUDE_SINGLETONS, true)
                        .build();
                uic.dispatcher().execute(operation, result -> {
                    String name = template.isEmpty() ? "Management Model" : template.last().value;
                    rootMbn = new ModelBrowserNode(template, name, RESOURCE);
                    tree.load(parseChildren(rootMbn, result, true));
                    detail.show(rootMbn);
                });
            });
        } else {
            // TODO Add error empty state
            logger.error("Illegal address: %s. Please specify a fully qualified address not ending with '*'", template);
        }
    }
}
