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
package org.jboss.hal.op.bootstrap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Subscription;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Endpoints;
import org.jboss.hal.env.Environment;
import org.jboss.hal.model.server.Server;
import org.jboss.hal.model.server.Standalone;
import org.jboss.hal.model.user.Current;
import org.jboss.hal.model.user.User;

import static java.util.Arrays.asList;
import static org.jboss.elemento.flow.Flow.sequential;

@ApplicationScoped
public class Bootstrap {

    @Inject Endpoints endpoints;
    @Inject Dispatcher dispatcher;
    @Inject Environment environment;
    @Inject @Standalone Server server;
    @Inject @Current User user;

    public Subscription<FlowContext> run() {
        return sequential(new FlowContext(), asList(
                new SetLogLevel(),
                new SelectEndpoint(endpoints),
                new SingleSignOnSupport(),
                new ReadEnvironment(dispatcher, environment, server, user)
        )).failFast(true);
    }
}
