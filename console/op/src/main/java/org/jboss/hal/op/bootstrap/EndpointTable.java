package org.jboss.hal.op.bootstrap;

import java.util.List;
import java.util.function.Consumer;

import org.jboss.elemento.Callback;
import org.jboss.elemento.IsElement;
import org.patternfly.component.table.Table;
import org.patternfly.component.table.Tr;
import org.patternfly.handler.SelectHandler;
import org.patternfly.icon.IconSets;
import org.patternfly.style.Size;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateFooter.emptyStateFooter;
import static org.patternfly.component.emptystate.EmptyStateHeader.emptyStateHeader;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.TableText.tableText;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Td.td;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.component.table.Wrap.fitContent;
import static org.patternfly.icon.IconSets.fas.pencilAlt;
import static org.patternfly.icon.IconSets.fas.trash;
import static org.patternfly.layout.bullseye.Bullseye.bullseye;

class EndpointTable implements IsElement<HTMLElement> {

    private final Table table;
    private final Tr emptyRow;
    private final Consumer<Endpoint> update;
    private final Consumer<Endpoint> delete;

    EndpointTable(Callback create, Consumer<Endpoint> update, Consumer<Endpoint> delete) {
        emptyRow = tr()
                .addData(td().colSpan(4)
                        .add(bullseye()
                                .add(emptyState().size(Size.sm)
                                        .addHeader(emptyStateHeader(2)
                                                .icon(IconSets.fas.ban())
                                                .text("No management interfaces found"))
                                        .addFooter(emptyStateFooter()
                                                .add(button().link().text("Add management interface")
                                                        .onClick((event, component) -> create.call()))))));
        this.update = update;
        this.delete = delete;
        table = table()
                .addHead(thead()
                        .addRow(tr()
                                .addHeader(th().textContent("Name"))
                                .addHeader(th().textContent("URL"))
                                .addHeader(th().screenReader("Edit"))
                                .addHeader(th().screenReader("Remove"))))
                .addBody(tbody());
    }

    @Override
    public HTMLElement element() {
        return table.element();
    }

    EndpointTable onSelect(SelectHandler<Tr> handler) {
        table.onSelect(handler);
        return this;
    }

    void select(String key) {
        table.select(key);
    }

    void show(List<Endpoint> endpoints) {
        if (endpoints.isEmpty()) {
            table.tbody().removeRows();
            table.tbody().addRow(emptyRow);
        } else {
            table.tbody().removeRows();
            table.tbody().addRows(endpoints, endpoint -> tr(endpoint.id)
                    .clickable()
                    .addData(td("Name").textContent(endpoint.name))
                    .addData(td("URL").textContent(endpoint.url))
                    .addData(td("Edit").wrap(fitContent)
                            .add(tableText()
                                    .add(button().plain().icon(pencilAlt())
                                            .onClick((event, component) -> update.accept(endpoint)))))
                    .addData(td("Remove").wrap(fitContent)
                            .add(tableText()
                                    .add(button().plain().icon(trash())
                                            .onClick((event, component) -> delete.accept(endpoint))))));
        }
    }
}
