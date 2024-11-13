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
package org.jboss.hal.core;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.elemento.Callback;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.MetadataRepository;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.StatementContextResolver;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.resources.Dataset.crudMessageName;
import static org.jboss.hal.resources.Dataset.crudMessageType;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.Severity.success;
import static org.patternfly.component.Severity.warning;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.alert.AlertDescription.alertDescription;
import static org.patternfly.component.alert.AlertGroup.toastAlertGroup;

@ApplicationScoped
public class CrudOperations {

    private final Environment environment;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final MetadataRepository metadataRepository;

    @Inject
    public CrudOperations(Environment environment, Dispatcher dispatcher, StatementContext statementContext,
            MetadataRepository metadataRepository) {
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.metadataRepository = metadataRepository;
    }

    // ------------------------------------------------------ update

    public void update(AddressTemplate template, List<Operation> operations, Callback onSuccess) {
        AddressTemplate resolvedTemplate = new StatementContextResolver(statementContext).resolve(template);
        String type = resolvedTemplate.last().key;
        String name = resolvedTemplate.last().value;
        if (!operations.isEmpty()) {
            if (operations.size() == 1) {
                dispatcher.execute(operations.get(0))
                        .then(result -> {
                            updateSuccess(type, name);
                            onSuccess.call();
                            return null;
                        })
                        .catch_(error -> {
                            updateError(type, name, error);
                            return null;
                        });
            } else {
                Composite composite = new Composite(operations);
                dispatcher.execute(composite)
                        .then(result -> {
                            updateSuccess(type, name);
                            onSuccess.call();
                            return null;
                        })
                        .catch_(error -> {
                            updateError(type, name, error);
                            return null;
                        });
            }
        } else {
            updateNoModifications(type, name);
            onSuccess.call();
        }
    }

    private void updateSuccess(String type, String name) {
        toastAlertGroup()
                .add(alert(success, "Update successful")
                        .addDescription(alertDescription()
                                .add(typeElement(type))
                                .run(ad -> {
                                    if (name != null) {
                                        ad.add(" ").add(nameElement(name));
                                    }
                                })
                                .add(" has been successfully updated.")));
    }

    private void updateNoModifications(String type, String name) {
        toastAlertGroup()
                .add(alert(warning, "Not modified")
                        .addDescription(alertDescription()
                                .add(typeElement(type))
                                .run(ad -> {
                                    if (name != null) {
                                        ad.add(" ").add(nameElement(name));
                                    }
                                })
                                .add(" has not been modified.")));
    }

    private void updateError(String type, String name, Object error) {
        // TODO Give details
        toastAlertGroup()
                .add(alert(danger, "Update failed")
                        .addDescription(alertDescription()
                                .add(typeElement(type))
                                .run(ad -> {
                                    if (name != null) {
                                        ad.add(" ").add(nameElement(name));
                                    }
                                })
                                .add(" could not  be updated.")));
    }

    // ------------------------------------------------------ internal

    private HTMLElement typeElement(String type) {
        String failSafeType = type == null ? "Management model" : new LabelBuilder().label(type);
        return span().data(crudMessageType, type).textContent(failSafeType).element();
    }

    private HTMLElement nameElement(String name) {
        String failSafeName = name == null ? "n/a" : name;
        return span().data(crudMessageName, name).textContent(failSafeName).element();
    }
}
