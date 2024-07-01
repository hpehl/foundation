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

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Level;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.env.Query;

import elemental2.promise.Promise;

import static org.jboss.elemento.logger.Level.INFO;

class SetLogLevel implements Task<FlowContext> {

    private static final Logger logger = Logger.getLogger(SetLogLevel.class.getName());
    private static final String LOG_LEVEL_PARAMETER = "log-level";

    @Override
    public Promise<FlowContext> apply(FlowContext context) {
        if (Query.getParameter(LOG_LEVEL_PARAMETER) != null) {
            String logLevel = Query.getParameter(LOG_LEVEL_PARAMETER);
            if (logLevel != null) {
                try {
                    Level level = Level.valueOf(logLevel.toUpperCase());
                    Logger.setLevel(level);
                } catch (IllegalArgumentException e) {
                    logger.error("Unknown log level '%s'", logLevel);
                    Logger.setLevel(INFO);
                }
            } else {
                Logger.setLevel(INFO);
            }
        } else {
            Logger.setLevel(INFO);
        }
        return context.resolve();
    }
}
