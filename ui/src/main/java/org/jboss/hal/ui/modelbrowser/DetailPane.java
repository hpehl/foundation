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
import org.patternfly.component.tree.TreeViewItem;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.hal.ui.modelbrowser.ModelBrowser.NODE;
import static org.patternfly.component.page.PageMainSection.pageMainSection;
import static org.patternfly.component.text.TextContent.textContent;
import static org.patternfly.style.Brightness.light;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.noPadding;

class DetailPane implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static DetailPane detailPane() {
        return new DetailPane();
    }

    // ------------------------------------------------------ instance

    private final HTMLElement root;
    private final HTMLElement header;
    private final HTMLElement description;

    DetailPane() {
        root = pageMainSection().background(light).css(modifier(noPadding))
                .add(textContent()
                        .add(header = h(1).element())
                        .add(description = p().element()))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ api

    void show(TreeViewItem treeViewItem) {
        Node node = treeViewItem.get(NODE);
        if (node != null) {
            header.textContent = node.name;
            description.textContent = "Detail pane not yet implemented!";
        } else {
            header.textContent = "n/a";
        }
    }
}
