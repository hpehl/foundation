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
package org.jboss.hal.meta;

import java.util.Objects;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_CONFIG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;

public class Placeholder {

    // ------------------------------------------------------ well-known placeholders

    public static final Placeholder DOMAIN_CONTROLLER = new Placeholder("domain.controller", HOST);
    public static final Placeholder SELECTED_DEPLOYMENT = new Placeholder("selected.deployment", DEPLOYMENT);
    public static final Placeholder SELECTED_HOST = new Placeholder("selected.host", HOST);
    public static final Placeholder SELECTED_PROFILE = new Placeholder("selected.profile", PROFILE);
    public static final Placeholder SELECTED_RESOURCE = new Placeholder("selected.resource");
    public static final Placeholder SELECTED_SERVER = new Placeholder("selected.server", SERVER);
    public static final Placeholder SELECTED_SERVER_CONFIG = new Placeholder("selected.server-config", SERVER_CONFIG);
    public static final Placeholder SELECTED_SERVER_GROUP = new Placeholder("selected.server-group", SERVER_GROUP);

    public final String name;
    public final String resource;

    Placeholder(String name) {
        this(name, null);
    }

    Placeholder(String name, String resource) {
        this.name = name;
        this.resource = resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        Placeholder that = (Placeholder) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return expression();
    }

    /** @return the {@code name} surrounded by "{" and "}" */
    public String expression() {
        return "{" + name + "}";
    }
}
