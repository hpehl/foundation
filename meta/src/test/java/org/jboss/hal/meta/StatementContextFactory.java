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

import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Version;

import static org.jboss.hal.env.OperationMode.DOMAIN;
import static org.jboss.hal.env.OperationMode.STANDALONE;
import static org.jboss.hal.meta.Placeholder.DOMAIN_CONTROLLER;
import static org.jboss.hal.meta.Placeholder.SELECTED_DEPLOYMENT;
import static org.jboss.hal.meta.Placeholder.SELECTED_HOST;
import static org.jboss.hal.meta.Placeholder.SELECTED_PROFILE;
import static org.jboss.hal.meta.Placeholder.SELECTED_RESOURCE;
import static org.jboss.hal.meta.Placeholder.SELECTED_SERVER;
import static org.jboss.hal.meta.Placeholder.SELECTED_SERVER_CONFIG;
import static org.jboss.hal.meta.Placeholder.SELECTED_SERVER_GROUP;

public class StatementContextFactory {

    public static StatementContext domainStatementContext() {
        return statementContext(true,
                new Placeholder[]{
                        DOMAIN_CONTROLLER, SELECTED_HOST,
                        SELECTED_PROFILE, SELECTED_SERVER_GROUP,
                        SELECTED_SERVER, SELECTED_SERVER_CONFIG,
                        SELECTED_DEPLOYMENT, SELECTED_RESOURCE},
                new String[]{
                        "primary", "secondary",
                        "full", "main-server-group",
                        "server1", "server2",
                        "hello-world", "bar"});
    }

    public static StatementContext standaloneStatementContext() {
        return statementContext(false,
                new Placeholder[]{SELECTED_DEPLOYMENT, SELECTED_RESOURCE},
                new String[]{"hello-world", "bar"});
    }

    public static StatementContext statementContext(boolean domain, Placeholder[] placeholders, String[] values) {
        StatementContext statementContext = new StatementContext(environment(domain));
        for (int i = 0; i < placeholders.length; i++) {
            statementContext.assign(placeholders[i], values[i]);
        }
        return statementContext;
    }

    public static StatementContext statementContext(String... pairs) {
        StatementContext statementContext = new StatementContext(environment(false));
        for (int i = 0; i < pairs.length; i += 2) {
            String placeholder = pairs[i];
            String value = pairs[i + 1];
            statementContext.assign(placeholder, value);
        }
        return statementContext;
    }

    private static Environment environment(boolean domain) {
        Environment environment = new Environment();
        environment.update("foo", "acme", "foo-product",
                Version.EMPTY_VERSION, Version.EMPTY_VERSION,
                domain ? DOMAIN : STANDALONE);
        return environment;
    }
}
