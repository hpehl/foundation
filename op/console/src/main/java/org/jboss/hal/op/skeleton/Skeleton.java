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
import org.jboss.hal.op.bootstrap.BootstrapErrorElement;
import org.jboss.hal.op.resources.Assets;
import org.jboss.hal.resources.Ids;
import org.patternfly.component.navigation.Navigation;
import org.patternfly.component.page.PageMain;
import org.patternfly.component.toolbar.ToolbarItem;
import org.patternfly.style.Variable;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.patternfly.component.backtotop.BackToTop.backToTop;
import static org.patternfly.component.brand.Brand.brand;
import static org.patternfly.component.page.Masthead.masthead;
import static org.patternfly.component.page.MastheadBrand.mastheadBrand;
import static org.patternfly.component.page.MastheadContent.mastheadContent;
import static org.patternfly.component.page.MastheadMain.mastheadMain;
import static org.patternfly.component.page.Page.page;
import static org.patternfly.component.page.PageMain.pageMain;
import static org.patternfly.component.page.PageMainBody.pageMainBody;
import static org.patternfly.component.page.PageMainSection.pageMainSection;
import static org.patternfly.component.skiptocontent.SkipToContent.skipToContent;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.style.Classes.brand;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.fullHeight;
import static org.patternfly.style.Classes.modifier;
import static org.patternfly.style.Classes.static_;
import static org.patternfly.style.Variable.componentVar;
import static org.patternfly.style.Variable.globalVar;
import static org.patternfly.style.Variables.Height;

public class Skeleton implements IsElement<HTMLElement> {

    private final HTMLElement root;
    private final ToolbarItem toolbarItem;
    private final PageMain pageMain;


    public Skeleton(Navigation navigation) {
        this();
        toolbarItem.add(navigation.element());
    }

    public Skeleton(BootstrapErrorElement bootstrapError) {
        this();
        Variable white = globalVar("BackgroundColor", "100");
        pageMain.add(pageMainSection()
                .limitWidth()
                .add(pageMainBody()
                        .add(div().style("background-color", "var(" + white.name + ")")
                                .add(bootstrapError))));
    }

    private Skeleton() {
        root = page()
                .addSkipToContent(skipToContent(Ids.MAIN_ID))
                .addMasthead(masthead()
                        .addMain(mastheadMain()
                                .addBrand(mastheadBrand(a("/"))
                                        .addBrand(brand(Assets.INSTANCE.logo().getSrc(), "HAL Management Console")
                                                .style(componentVar(component(brand), Height).name, "36px"))))
                        .addContent(mastheadContent()
                                .addToolbar(toolbar().css(modifier(fullHeight), modifier(static_))
                                        .addContent(toolbarContent()
                                                .add(toolbarItem = toolbarItem().css(modifier("overflow-container")))))))
                .addMain(pageMain = pageMain(Ids.MAIN_ID))
                .add(backToTop()
                        .scrollableSelector(By.id(Ids.MAIN_ID)))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
