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

import org.jboss.elemento.logger.Logger;
import org.patternfly.component.button.Button;
import org.patternfly.component.modal.Modal;
import org.patternfly.component.table.Table;
import org.patternfly.component.table.Tr;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.icon.IconSets;
import org.patternfly.style.Size;

import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.RejectCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;

import static org.jboss.elemento.Elements.isVisible;
import static org.jboss.elemento.Elements.setVisible;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.modal.ModalFooter.modalFooter;
import static org.patternfly.component.modal.ModalHeader.modalHeader;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.TableText.tableText;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.component.table.Wrap.fitContent;
import static org.patternfly.component.toolbar.Toolbar.toolbar;
import static org.patternfly.component.toolbar.ToolbarContent.toolbarContent;
import static org.patternfly.component.toolbar.ToolbarItem.toolbarItem;
import static org.patternfly.icon.IconSets.fas.pencilAlt;
import static org.patternfly.icon.IconSets.fas.trash;
import static org.patternfly.layout.bullseye.Bullseye.bullseye;
import static org.patternfly.style.Size.md;

class EndpointModal {

    private static final Logger logger = Logger.getLogger(EndpointModal.class.getName());

    private final EndpointStorage storage;
    private final Button ok;
    private final Button cancel;
    private final EndpointForm form;
    private final Modal modal;
    private final Table table;
    private final Tr emptyRow;
    private final Toolbar toolbar;
    private Endpoint endpoint;
    private ResolveCallbackFn<Endpoint> resolve;
    private RejectCallbackFn reject;

    EndpointModal(EndpointStorage storage) {
        this.storage = storage;
        this.endpoint = null;

        ok = button("Connect")
                .primary()
                .disabled()
                .onClick((event, component) -> saveOrConnect());
        cancel = button("Cancel")
                .link()
                .onClick((event, component) -> cancel());
        toolbar = toolbar()
                .addContent(toolbarContent()
                        .addItem(toolbarItem()
                                .add(button().primary().text("Add").onClick((event, component) -> newEndpoint()))));

        form = new EndpointForm(storage);
        emptyRow = tr()
                .addData(td().colSpan(4)
                        .add(bullseye()
                                .add(emptyState().size(Size.sm)
                                        .addHeader(emptyStateHeader(2)
                                                .icon(IconSets.fas.ban())
                                                .text("No management interfaces found"))
                                        .addBody(emptyStateBody().textContent(
                                                "Please add a new management interface."))
                                        .addFooter(emptyStateFooter()
                                                .add(button().link().text("Add management interface")
                                                        .onClick((event, component) -> newEndpoint()))))));
        table = table().compact()
                .onSelect((e, tr, selected) -> {
                    ok.disabled(!selected);
                    if (selected) {
                        endpoint = storage.get(tr.key);
                    }
                })
                .addHead(thead()
                        .addRow(tr()
                                .addHeader(th().textContent("Name"))
                                .addHeader(th().textContent("URL"))
                                .addHeader(th().screenReader("Edit"))
                                .addHeader(th().screenReader("Remove"))))
                .addBody(tbody());

        modal = modal()
                .size(md)
                .hideClose()
                .autoClose(false)
                .addHeader(modalHeader()
                        .addTitle("Connect to a management interface")
                        .addDescription(
                                "HAL is running in standalone mode. In order to proceed, you must connect to a management interface. Pick one from the list below or add a new one."))
                .addBody(modalBody()
                        .add(form)
                        .add(toolbar)
                        .add(table))
                .addFooter(modalFooter()
                        .addButton(ok)
                        .addButton(cancel))
                .appendToBody();

        setVisible(form, false);
        setVisible(toolbar, false);
        setVisible(table, false);
    }

    Promise<Endpoint> open() {
        return new Promise<>((resolve, reject) -> {
            this.resolve = resolve;
            this.reject = reject;
            showEndpoints();
            modal.open();
        });
    }

    // ------------------------------------------------------ internal

    private void showEndpoints() {
        if (storage.isEmpty()) {
            noEndpoints();
        } else {
            existingEndpoints(storage.endpoints());
        }
    }

    private void noEndpoints() {
        table.tbody().removeRows();
        table.tbody().addRow(emptyRow);
        ok.text("Connect").disabled(true);

        setVisible(form, false);
        setVisible(toolbar, false);
        setVisible(table, true);
        setVisible(cancel, false);
    }

    private void existingEndpoints(Iterable<Endpoint> endpoints) {
        table.tbody().removeRows();
        table.tbody().addRows(endpoints, endpoint -> tr(endpoint.id)
                .clickable()
                .addData(td("Name").textContent(endpoint.name))
                .addData(td("URL").textContent(endpoint.url))
                .addData(td("Edit").wrap(fitContent)
                        .add(tableText()
                                .add(button().plain().icon(pencilAlt())
                                        .onClick((event, component) -> edit(endpoint)))))
                .addData(td("Remove").wrap(fitContent)
                        .add(tableText()
                                .add(button().plain().icon(trash())
                                        .onClick((event, component) -> remove(endpoint))))));
        ok.text("Connect").disabled(true); // will be enabled on endpoint selection

        setVisible(form, false);
        setVisible(toolbar, true);
        setVisible(table, true);
        setVisible(cancel, false);
    }

    private void newEndpoint() {
        form.reset();
        form.show(null);
        ok.text("Add").disabled(false);

        setVisible(form, true);
        setVisible(toolbar, false);
        setVisible(table, false);
        setVisible(cancel, true);
    }

    private void edit(Endpoint endpoint) {
        form.reset();
        form.show(endpoint);
        ok.text("Save").disabled(false);

        setVisible(form, true);
        setVisible(toolbar, false);
        setVisible(table, false);
        setVisible(cancel, true);
    }

    private void saveOrConnect() {
        if (isVisible(form)) {
            if (form.isValid()) {
                Endpoint endpoint = form.endpoint();
                storage.add(endpoint);
                showEndpoints();
                table.select(endpoint.id);
            }
        } else {
            if (endpoint != null) {
                modal.close();
                resolve.onInvoke(endpoint);
            } else {
                modal.close();
                String error = "Cannot resolve endpoint modal. No selected endpoint!";
                logger.error(error);
                reject.onInvoke(error);
            }
        }
    }

    private void remove(Endpoint endpoint) {
        storage.remove(endpoint.id);
        showEndpoints();
    }

    private void cancel() {
        showEndpoints();
    }
}
