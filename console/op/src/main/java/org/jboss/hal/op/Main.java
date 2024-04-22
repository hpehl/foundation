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

import org.jboss.elemento.By;
import org.jboss.elemento.router.PlaceManager;
import org.jboss.elemento.router.RoutesImpl;
import org.kie.j2cl.tools.processors.annotations.GWT3EntryPoint;

import static org.jboss.elemento.Elements.body;
import static org.jboss.hal.op.Environment.env;

@SuppressWarnings("unused")
public class Main {

    static final String MAIN_ID = "hop-main-id";

    @GWT3EntryPoint
    public void onModuleLoad() {
        PlaceManager placeManager = new PlaceManager()
                .base(env().base)
                .root(By.id(MAIN_ID))
                .title(title -> "HAL • " + title)
                .notFound(NotFound::new)
                .register(RoutesImpl.INSTANCE.places());
        body().add(new Skeleton(MAIN_ID));
        placeManager.start();
    }
}
