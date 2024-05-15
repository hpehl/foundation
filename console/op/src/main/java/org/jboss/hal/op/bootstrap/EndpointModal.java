package org.jboss.hal.op.bootstrap;

import org.jboss.elemento.Elements;
import org.patternfly.component.button.Button;
import org.patternfly.component.modal.Modal;
import org.patternfly.component.modal.ModalBody;
import org.patternfly.component.table.Table;
import org.patternfly.component.table.Tbody;
import org.patternfly.component.table.Tr;
import org.patternfly.component.toolbar.Toolbar;
import org.patternfly.icon.IconSets;
import org.patternfly.style.Size;

import static org.jboss.elemento.Elements.isAttached;
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

    private final EndpointStorage storage;
    private final Button ok;
    private final Button cancel;
    private final EndpointForm form;
    private final Modal modal;
    private final ModalBody modalBody;
    private final Table table;
    private final Tbody tbody;
    private final Tr emptyRow;
    private final Toolbar toolbar;

    EndpointModal(EndpointStorage storage) {
        this.storage = storage;

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
                                                        .onClick((event, component) -> addSaveOrConnect()))))));

        toolbar = toolbar()
                .addContent(toolbarContent()
                        .addItem(toolbarItem()
                                .add(button().primary().text("Add").onClick((event, component) -> addSaveOrConnect()))));

        table = table().compact()
                .addHead(thead()
                        .addRow(tr()
                                .addHeader(th().textContent("Name"))
                                .addHeader(th().textContent("URL"))
                                .addHeader(th().screenReader("Edit"))
                                .addHeader(th().screenReader("Remove"))))
                .addBody(tbody = tbody());

        modal = modal()
                .size(md)
                .hideClose()
                .autoClose(false)
                .addHeader(modalHeader()
                        .addTitle("Connect to a management interface")
                        .addDescription(
                                "HAL is running in standalone mode. In order to proceed, you must connect to a management interface. Pick one from the list below or add a new one."))
                .addBody(modalBody = modalBody())
                .addFooter(modalFooter()
                        .addButton(ok = button("Connect")
                                .primary()
                                .onClick((event, component) -> addSaveOrConnect()))
                        .addButton(cancel = button("Cancel")
                                .link()
                                .onClick((event, component) -> cancel())))
                .appendToBody();

        form = new EndpointForm();
    }

    void open() {
        showEndpoints();
        modal.open();
    }

    // ------------------------------------------------------ state

    private void showEndpoints() {
        if (storage.isEmpty()) {
            noEndpoints();
        } else {
            existingEndpoints(storage.endpoints());
        }
    }

    private void noEndpoints() {
        reset();
        tbody.addRow(emptyRow);
        modalBody.add(table);
        ok.text("Connect").disabled(true);
        Elements.setVisible(cancel, false);
    }

    private void existingEndpoints(Iterable<Endpoint> endpoints) {
        reset();
        tbody.addRows(storage.endpoints(), endpoint -> tr()
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
        modalBody
                .add(toolbar)
                .add(table);
        ok.text("Connect").disabled(true); // will be enabled on endpoint selection
        Elements.setVisible(cancel, false);
    }

    private void newEndpoint() {
        reset();
        form.show(null);
        modalBody.add(form);
        ok.text("Add").disabled(false);
        Elements.setVisible(cancel, true);
    }

    // ------------------------------------------------------ event handler

    private void addSaveOrConnect() {
        if (isAttached(form)) {
            if (form.isValid()) {
                storage.add(form.endpoint());
                showEndpoints();
            }
        } else {
            newEndpoint();
        }
    }

    private void edit(Endpoint endpoint) {
        reset();
        form.show(endpoint);
        modalBody.add(form);
        ok.text("Save").disabled(false);
        Elements.setVisible(cancel, true);
    }

    private void remove(Endpoint endpoint) {
        storage.remove(endpoint.name);
        showEndpoints();
    }

    private void cancel() {
        showEndpoints();
    }

    // ------------------------------------------------------ helper

    private void reset() {
        Elements.removeChildrenFrom(modalBody.element());
        tbody.clearRows();
        form.reset();
    }
}
