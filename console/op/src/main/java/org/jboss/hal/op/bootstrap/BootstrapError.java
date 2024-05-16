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

import elemental2.promise.Promise;

public class BootstrapError {

    public enum Failure {

        NO_ENDPOINT_SPECIFIED,

        NO_ENDPOINT_FOUND,

        NOT_AN_ENDPOINT,

        NETWORK_ERROR,

        UNKNOWN,
    }

    public static final BootstrapError UNKNOWN = new BootstrapError(Failure.UNKNOWN, null);

    static Promise<FlowContext> fail(FlowContext context, BootstrapError.Failure failure, String data) {
        context.push(new BootstrapError(failure, data));
        return context.reject(failure);
    }

    private final Failure failure;
    private final String data;

    BootstrapError(Failure failure, String data) {
        this.failure = failure;
        this.data = data;
    }

    public Failure failure() {
        return failure;
    }

    public String data() {
        return data;
    }
}
