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
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.env.Endpoints;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Urls;

import elemental2.dom.Request;
import elemental2.dom.RequestInit;
import elemental2.dom.URLSearchParams;
import elemental2.promise.Promise;
import elemental2.webstorage.Storage;
import elemental2.webstorage.WebStorageWindow;

import static elemental2.dom.DomGlobal.fetch;
import static elemental2.dom.DomGlobal.location;
import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.op.bootstrap.BootstrapError.fail;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NETWORK_ERROR;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NOT_AN_ENDPOINT;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NO_ENDPOINT_FOUND;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NO_ENDPOINT_GIVEN;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.NO_LOCAL_STORAGE;
import static org.jboss.hal.op.bootstrap.BootstrapError.Failure.UNKNOWN;

public class SelectEndpoint implements Task<FlowContext> {

    private static final Logger logger = Logger.getLogger(SelectEndpoint.class.getName());
    private static final String CONNECT_PARAMETER = "connect";
    private final Endpoints endpoints;

    public SelectEndpoint(Endpoints endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public Promise<FlowContext> apply(FlowContext context) {
        if (location.search.isEmpty()) {
            return ping(context, "", false);
        } else {
            URLSearchParams query = new URLSearchParams(location.search);
            if (!query.has(CONNECT_PARAMETER)) {
                return ping(context, "", false);
            } else {
                String connect = query.get(CONNECT_PARAMETER);
                if (!connect.isEmpty()) {
                    if (connect.contains("://")) {
                        return ping(context, connect, true);
                    } else {
                        Storage localStorage = WebStorageWindow.of(window).localStorage;
                        if (localStorage != null) {
                            String endpoint = localStorage.getItem(Ids.endpoint(connect));
                            if (endpoint != null) {
                                return ping(context, endpoint, true);
                            } else {
                                return fail(context, NO_ENDPOINT_FOUND, connect);
                            }
                        } else {
                            return fail(context, NO_LOCAL_STORAGE, connect);
                        }
                    }
                } else {
                    return fail(context, NO_ENDPOINT_GIVEN, CONNECT_PARAMETER);
                }
            }
        }
    }

    private Promise<FlowContext> ping(FlowContext context, String url, boolean failFast) {
        String failSafeUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        String managementEndpoint = failSafeUrl + Urls.MANAGEMENT;
        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setMode("cors");
        init.setCredentials("include");
        Request request = new Request(managementEndpoint, init);

        logger.debug("Ping %s...", managementEndpoint);
        return fetch(request)
                .then(response -> {
                    if (response.status == 200) {
                        logger.debug("200. Validate response...");
                        return response.text().then(text -> {
                            if (text.contains(ModelDescriptionConstants.MANAGEMENT_MAJOR_VERSION)) {
                                logger.debug("Endpoint %s is valid!", url);
                                endpoints.init(failSafeUrl);
                                if (!url.isEmpty()) {
                                    logger.info("Select endpoint: %s", url);
                                }
                                return context.resolve();
                            } else {
                                logger.debug("Not a valid endpoint: %s", url);
                                if (failFast) {
                                    context.push(new BootstrapError(NOT_AN_ENDPOINT, url));
                                    return context.reject("failed");
                                } else {
                                    // TODO Add or select endpoint
                                    context.push(new BootstrapError(UNKNOWN, "Endpoint selection not yet implemented!"));
                                    return context.reject("failed");
                                }
                            }
                        });
                    } else {
                        logger.debug("%d. Not a valid endpoint: %s", response.status, url);
                        if (failFast) {
                            context.push(new BootstrapError(NOT_AN_ENDPOINT, url));
                            return context.reject("failed");
                        } else {
                            // TODO Add or select endpoint
                            context.push(new BootstrapError(UNKNOWN, "Endpoint selection not yet implemented!"));
                            return context.reject("failed");
                        }
                    }
                })
                .catch_(error -> {
                    if (context.emptyStack()) {
                        return fail(context, NETWORK_ERROR, String.valueOf(error));
                    } else {
                        return context.reject(error);
                    }
                });
    }
}
