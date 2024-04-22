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
package org.jboss.hal.op;

import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Route;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;

import static org.jboss.hal.op.Environment.env;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.component.page.PageMainSection.pageMainSection;
import static org.patternfly.style.Brightness.light;

@Route("/")
public class HomePage implements Page {

    @Override
    public Iterable<HTMLElement> elements() {
        return singletonList(pageMainSection()
                .background(light)
                .add(descriptionList()
                        .addGroup(descriptionListGroup()
                                .addTerm(descriptionListTerm("ID"))
                                .addDescription(descriptionListDescription(env().id))
                                .addTerm(descriptionListTerm("Name"))
                                .addDescription(descriptionListDescription(env().name))
                                .addTerm(descriptionListTerm("Version"))
                                .addDescription(descriptionListDescription(env().version))
                                .addTerm(descriptionListTerm("Mode"))
                                .addDescription(descriptionListDescription(env().mode))))
                .element());
    }
}
