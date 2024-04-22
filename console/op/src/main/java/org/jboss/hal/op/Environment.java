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

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Environment {

    // ------------------------------------------------------ instance

    public final String id;
    public final String name;
    public final String version;
    public final String base;
    public final String mode;

    Environment() {
        this.id = System.getProperty("environment.id");
        this.name = System.getProperty("environment.name");
        this.version = System.getProperty("environment.version");
        this.base = System.getProperty("environment.base");
        this.mode = System.getProperty("environment.mode");
    }
}
