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
import org.junit.jupiter.api.Test;

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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatementContextResolverTest {

    @Test
    void nil() {
        assertThrows(IllegalArgumentException.class, () -> new StatementContextResolver(null));
    }

    @Test
    void empty() {
        StatementContextResolver resolver = new StatementContextResolver(statementContext());
        assertEquals("a=b", resolver.resolve(AddressTemplate.of("a=b")).template);
    }

    @Test
    void resolveValue() {
        StatementContextResolver resolver = new StatementContextResolver(statementContext("a", "a", "b", "b"));
        assertEquals("a=a", resolver.resolve(AddressTemplate.of("a={a}")).template);
        assertEquals("{a}=a", resolver.resolve(AddressTemplate.of("{a}=a")).template);
        assertEquals("{a}=a", resolver.resolve(AddressTemplate.of("{a}={a}")).template);
        assertEquals("b=b", resolver.resolve(AddressTemplate.of("b={b}")).template);
        assertEquals("a=a/b=b", resolver.resolve(AddressTemplate.of("a={a}/b={b}")).template);
        assertEquals("{a}=a/{b}=b", resolver.resolve(AddressTemplate.of("{a}={a}/{b}={b}")).template);
    }

    @Test
    void resolveKnownPlaceholdersDomain() {
        StatementContextResolver resolver = new StatementContextResolver(statementContext(true,
                new Placeholder[]{
                        DOMAIN_CONTROLLER, SELECTED_DEPLOYMENT, SELECTED_HOST, SELECTED_PROFILE,
                        SELECTED_SERVER, SELECTED_SERVER_CONFIG, SELECTED_SERVER_GROUP,
                        SELECTED_RESOURCE},
                new String[]{"primary", "hello-world", "secondary", "full", "server1", "server2", "main-server-group", "bar"}));
        assertEquals("host=primary", resolver.resolve(AddressTemplate.of("{domain.controller}")).template);
        assertEquals("deployment=hello-world", resolver.resolve(AddressTemplate.of("{selected.deployment}")).template);
        assertEquals("host=secondary", resolver.resolve(AddressTemplate.of("{selected.host}")).template);
        assertEquals("profile=full", resolver.resolve(AddressTemplate.of("{selected.profile}")).template);
        assertEquals("server=server1", resolver.resolve(AddressTemplate.of("{selected.server}")).template);
        assertEquals("server-config=server2", resolver.resolve(AddressTemplate.of("{selected.server-config}")).template);
        assertEquals("server-group=main-server-group",
                resolver.resolve(AddressTemplate.of("{selected.server-group}")).template);
        assertEquals("foo=bar", resolver.resolve(AddressTemplate.of("foo={selected.resource}")).template);
    }

    @Test
    void resolveKnownPlaceholdersStandalone() {
        StatementContextResolver resolver = new StatementContextResolver(statementContext(false,
                new Placeholder[]{
                        DOMAIN_CONTROLLER, SELECTED_DEPLOYMENT, SELECTED_HOST, SELECTED_PROFILE,
                        SELECTED_SERVER, SELECTED_SERVER_CONFIG, SELECTED_SERVER_GROUP,
                        SELECTED_RESOURCE},
                new String[]{"primary", "hello-world", "secondary", "full", "server1", "server2", "main-server-group", "bar"}));
        assertEquals("a=b", resolver.resolve(AddressTemplate.of("{domain.controller}/a=b")).template);
        assertEquals("deployment=hello-world", resolver.resolve(AddressTemplate.of("{selected.deployment}")).template);
        assertEquals("a=b", resolver.resolve(AddressTemplate.of("{selected.host}/a=b")).template);
        assertEquals("a=b", resolver.resolve(AddressTemplate.of("{selected.profile}/a=b")).template);
        assertEquals("a=b", resolver.resolve(AddressTemplate.of("{selected.server}/a=b")).template);
        assertEquals("a=b", resolver.resolve(AddressTemplate.of("{selected.server-config}/a=b")).template);
        assertEquals("a=b", resolver.resolve(AddressTemplate.of("{selected.server-group}/a=b")).template);
        assertEquals("foo=bar", resolver.resolve(AddressTemplate.of("foo={selected.resource}")).template);
    }

    private StatementContext statementContext(String... pairs) {
        StatementContext statementContext = new StatementContext(environment(false));
        for (int i = 0; i < pairs.length; i += 2) {
            String placeholder = pairs[i];
            String value = pairs[i + 1];
            statementContext.assign(placeholder, value);
        }
        return statementContext;
    }

    private StatementContext statementContext(boolean domain, Placeholder[] placeholders, String[] values) {
        StatementContext statementContext = new StatementContext(environment(domain));
        for (int i = 0; i < placeholders.length; i++) {
            statementContext.assign(placeholders[i], values[i]);
        }
        return statementContext;
    }

    private Environment environment(boolean domain) {
        Environment environment = new Environment();
        environment.update("name", "org", "product",
                Version.EMPTY_VERSION, Version.EMPTY_VERSION,
                domain ? DOMAIN : STANDALONE);
        return environment;
    }
}