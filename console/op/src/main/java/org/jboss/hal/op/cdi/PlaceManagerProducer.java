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
package org.jboss.hal.op.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.jboss.elemento.By;
import org.jboss.elemento.router.PlaceManager;
import org.jboss.elemento.router.Routes;
import org.jboss.hal.op.Environment;
import org.jboss.hal.op.NotFound;

import static org.jboss.hal.op.Constants.MAIN_ID;

public class PlaceManagerProducer {

    @Inject Environment environment;
    @Inject Routes routes;

    @Produces
    @ApplicationScoped
    public PlaceManager placeManager() {
        return new PlaceManager()
                .base(environment.base)
                .root(By.id(MAIN_ID))
                .title(title -> "HAL • " + title)
                .notFound(NotFound::new)
                .register(routes.places());
    }
}
