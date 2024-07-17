/*
 *  Copyright 2022 Red Hat
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

import jakarta.inject.Inject;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.env.Settings;
import org.jboss.hal.resources.Names;

import elemental2.promise.Promise;

import static org.jboss.hal.env.Settings.DEFAULT_COLLECT_USER_DATA;
import static org.jboss.hal.env.Settings.DEFAULT_LOCALE;
import static org.jboss.hal.env.Settings.DEFAULT_PAGE_SIZE;
import static org.jboss.hal.env.Settings.DEFAULT_POLL_TIME;
import static org.jboss.hal.env.Settings.Key.COLLECT_USER_DATA;
import static org.jboss.hal.env.Settings.Key.LOCALE;
import static org.jboss.hal.env.Settings.Key.PAGE_SIZE;
import static org.jboss.hal.env.Settings.Key.POLL;
import static org.jboss.hal.env.Settings.Key.POLL_TIME;
import static org.jboss.hal.env.Settings.Key.RUN_AS;
import static org.jboss.hal.env.Settings.Key.TITLE;

/**
 * Loads the settings. Please make sure this is one of the last bootstrap function. This function loads the run-as role which is
 * then used by the dispatcher. But all previous bootstrap functions must not have a run-as role in the dispatcher.
 */
public final class LoadSettings implements Task<FlowContext> {

    private static final Logger logger = Logger.getLogger(LoadSettings.class.getName());
    private final Settings settings;

    @Inject
    public LoadSettings(Settings settings) {
        this.settings = settings;
    }

    @Override
    public Promise<FlowContext> apply(final FlowContext context) {
        settings.load(TITLE, Names.BROWSER_DEFAULT_TITLE);
        settings.load(COLLECT_USER_DATA, DEFAULT_COLLECT_USER_DATA);
        settings.load(LOCALE, DEFAULT_LOCALE);
        settings.load(PAGE_SIZE, DEFAULT_PAGE_SIZE);
        settings.load(POLL, true);
        settings.load(POLL_TIME, DEFAULT_POLL_TIME);
        settings.load(RUN_AS, null);
        logger.info("Settings: %s", settings);
        return Promise.resolve(context);
    }
}
