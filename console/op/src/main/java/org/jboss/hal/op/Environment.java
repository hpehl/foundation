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

@SuppressWarnings("unused")
public class Environment {

    // ------------------------------------------------------ factory

    public static Environment env() {
        return instance;
    }

    private static Environment instance;

    static {
        instance = new Environment();
    }

    // ------------------------------------------------------ instance

    public final String applicationId;
    public final String applicationName;
    public final String applicationVersion;
    public final String base;
    public final String mode;

    private Environment() {
        this.applicationId = System.getProperty("environment.application.id");
        this.applicationName = System.getProperty("environment.application.name");
        this.applicationVersion = System.getProperty("environment.application.version");
        this.base = System.getProperty("environment.base");
        this.mode = System.getProperty("environment.mode");
    }
}
