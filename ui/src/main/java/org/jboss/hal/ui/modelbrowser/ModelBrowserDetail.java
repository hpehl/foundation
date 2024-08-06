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

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.page.PageMainBreadcrumb;
import org.patternfly.component.page.PageMainSection;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLHeadingElement;
import elemental2.dom.HTMLParagraphElement;

import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.resources.HalClasses.content;
import static org.jboss.hal.resources.HalClasses.detail;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.modelBrowser;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.FOLDER;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.SINGLETON_FOLDER;
import static org.patternfly.component.breadcrumb.Breadcrumb.breadcrumb;
import static org.patternfly.component.breadcrumb.BreadcrumbItem.breadcrumbItem;
import static org.patternfly.component.page.PageMainBreadcrumb.pageMainBreadcrumb;
import static org.patternfly.component.page.PageMainGroup.pageMainGroup;
import static org.patternfly.component.page.PageMainSection.pageMainSection;
import static org.patternfly.component.text.TextContent.textContent;
import static org.patternfly.style.Brightness.light;
import static org.patternfly.style.Sticky.top;

class ModelBrowserDetail implements IsElement<HTMLElement> {

    private final UIContext uic;
    private final HTMLElement root;
    private final PageMainBreadcrumb pageMainBreadcrumb;
    private final HTMLContainerBuilder<HTMLHeadingElement> header;
    private final HTMLContainerBuilder<HTMLParagraphElement> description;
    private final PageMainSection pageMainSection;
    ModelBrowserTree tree;

    ModelBrowserDetail(UIContext uic) {
        this.uic = uic;
        this.root = div().css(halComponent(modelBrowser, detail))
                .add(pageMainGroup()
                        .sticky(top)
                        .addSection(pageMainBreadcrumb = pageMainBreadcrumb())
                        .addSection(pageMainSection().background(light)
                                .add(textContent()
                                        .add(header = h(1, ""))
                                        .add(description = p().textContent("")))))
                .add(pageMainSection = pageMainSection().css(halComponent(modelBrowser, detail, content)))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    void show(ModelBrowserNode mbn) {
        clear();
        fillBreadcrumb(mbn);
        adjustHeader(mbn);
        switch (mbn.type) {
            case SINGLETON_FOLDER:
            case FOLDER:
                pageMainSection.add(new ResourcesElement(uic, tree, mbn));
                break;
            case SINGLETON_RESOURCE:
            case RESOURCE:
                pageMainSection.add(new ResourceElement(uic, mbn,
                        metadata -> description.textContent(metadata.resourceDescription.description())));
                break;
        }
    }

    private void adjustHeader(ModelBrowserNode mbn) {
        if (mbn.type == SINGLETON_FOLDER) {
            header.add("Singleton child resources of ").add(code().textContent(mbn.name));
        } else if (mbn.type == FOLDER) {
            header.add("Child resources of ").add(code().textContent(mbn.name));
        } else {
            header.add(mbn.name);
        }
    }

    private void fillBreadcrumb(ModelBrowserNode mbn) {
        if (mbn.template.isEmpty()) {
            pageMainBreadcrumb.add(breadcrumb()
                    .addItem(breadcrumbItem("root", "/")));
        } else {
            pageMainBreadcrumb.add(breadcrumb()
                    .addItems(mbn.template, segment -> breadcrumbItem(Id.build(segment.key, segment.value),
                            segment.key + "=" + segment.value)));
        }
    }

    private void clear() {
        removeChildrenFrom(pageMainBreadcrumb);
        removeChildrenFrom(header);
        removeChildrenFrom(description);
        removeChildrenFrom(pageMainSection);
    }
}
