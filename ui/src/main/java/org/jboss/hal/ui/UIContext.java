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
package org.jboss.hal.ui;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.MetadataRepository;
import org.jboss.hal.meta.StatementContext;

/** Holds common classes needed by UI elements. Use this class to keep the method signatures clean and tidy. */
@ApplicationScoped
public class UIContext {

    public final Dispatcher dispatcher;
    public final MetadataRepository metadataRepository;
    public final StatementContext statementContext;

    @Inject
    public UIContext(Dispatcher dispatcher, MetadataRepository metadataRepository, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.metadataRepository = metadataRepository;
        this.statementContext = statementContext;
    }
}
