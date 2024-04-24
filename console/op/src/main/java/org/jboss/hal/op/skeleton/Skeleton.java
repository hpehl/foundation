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
package org.jboss.hal.op.skeleton;

import org.jboss.elemento.By;
import org.jboss.elemento.IsElement;
import org.patternfly.component.navigation.Navigation;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.hal.op.Ids.MAIN_ID;
import static org.jboss.hal.op.resources.Asserts.INSTANCE;
import static org.patternfly.component.backtotop.BackToTop.backToTop;
import static org.patternfly.component.brand.Brand.brand;
import static org.patternfly.component.page.Masthead.masthead;
import static org.patternfly.component.page.MastheadBrand.mastheadBrand;
import static org.patternfly.component.page.MastheadContent.mastheadContent;
import static org.patternfly.component.page.MastheadMain.mastheadMain;
import static org.patternfly.component.page.Page.page;
import static org.patternfly.component.page.PageMain.pageMain;
import static org.patternfly.component.skiptocontent.SkipToContent.skipToContent;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.fullHeight;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.static_;
import static org.patternfly.style.Variable.componentVar;
import static org.patternfly.style.Variables.Height;

public class Skeleton implements IsElement<HTMLElement> {

    private final HTMLElement root;

    public Skeleton(Navigation navigation) {
        root = page()
                .addSkipToContent(skipToContent(MAIN_ID))
                .addMasthead(masthead()
                        .addMain(mastheadMain()
                                .addBrand(mastheadBrand(a("/"))
                                        .addBrand(brand(INSTANCE.logo().getSrc(), "HAL Management Console")
                                                .style(componentVar(component(Classes.brand), Height).name, "36px"))))
                        .addContent(mastheadContent()
                                .addToolbar(toolbar().css(modifier(fullHeight), modifier(static_))
                                        .addContent(toolbarContent()
                                                .add(toolbarItem().css(modifier("overflow-container"))
                                                        .add(navigation))))))
                .addMain(pageMain(MAIN_ID))
                .add(backToTop()
                        .scrollableSelector(By.id(MAIN_ID)))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}