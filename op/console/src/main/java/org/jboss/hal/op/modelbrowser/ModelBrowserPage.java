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
package org.jboss.hal.op.modelbrowser;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.elemento.router.Route;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.UIContext;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static org.jboss.hal.ui.modelbrowser.ModelBrowser.modelBrowser;
import static org.patternfly.component.page.PageMainSection.pageMainSection;
import static org.patternfly.style.Brightness.light;

@Dependent
@Route("/model-browser")
public class ModelBrowserPage implements Page {

    private final UIContext uic;

    @Inject
    public ModelBrowserPage(UIContext uic) {
        this.uic = uic;
    }

    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        return singletonList(
                pageMainSection().background(light)
                        .add(modelBrowser(uic, AddressTemplate.root()))
                        .element());
    }
}
