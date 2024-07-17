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
package org.jboss.hal.ui.modelbrowser;

import java.util.List;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.flow.Flow;
import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.ui.UIContext;
import org.patternfly.style.Variable;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLHeadingElement;
import elemental2.dom.HTMLParagraphElement;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.patternfly.component.list.DescriptionList.descriptionList;
import static org.patternfly.component.list.DescriptionListDescription.descriptionListDescription;
import static org.patternfly.component.list.DescriptionListGroup.descriptionListGroup;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.component.text.TextContent.textContent;
import static org.patternfly.style.Breakpoint._2xl;
import static org.patternfly.style.Breakpoint.default_;
import static org.patternfly.style.Breakpoint.lg;
import static org.patternfly.style.Breakpoint.md;
import static org.patternfly.style.Breakpoint.sm;
import static org.patternfly.style.Breakpoint.xl;
import static org.patternfly.style.Breakpoints.breakpoints;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.main;
import static org.patternfly.style.Classes.page;
import static org.patternfly.style.Classes.section;
import static org.patternfly.style.Variable.componentVar;

class DetailPane implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    public static DetailPane detailPane(UIContext uic) {
        return new DetailPane(uic);
    }

    // ------------------------------------------------------ instance

    private final UIContext uic;
    private final HTMLElement root;
    private final HTMLContainerBuilder<HTMLHeadingElement> header;
    private final HTMLContainerBuilder<HTMLParagraphElement> description;
    private final HTMLContainerBuilder<HTMLDivElement> content;

    DetailPane(UIContext uic) {
        this.uic = uic;
        Variable paddingTop = componentVar(component(page, main, section), "PaddingTop");
        Variable paddingBottom = componentVar(component(page, main, section), "PaddingBottom");
        this.root = div()
                .add(textContent()
                        .style("padding-block-end", paddingBottom.asVar())
                        .add(header = h(1))
                        .add(description = p()))
                .add(content = div()
                        .style("padding-block-start", paddingTop.asVar()))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ api

    void show(Node node) {
        clear();
        switch (node.type) {
            case FOLDER:
                header.add("Child resources of ").add(code().textContent(node.name));
                break;
            case SINGLETON_PARENT:
                header.add("Singleton child resources of ").add(code().textContent(node.name));
                break;
            case SINGLETON_RESOURCE:
            case RESOURCE:
                showResource(node);
                break;
        }
    }

    // ------------------------------------------------------ internal

    private void clear() {
        removeChildrenFrom(header);
        removeChildrenFrom(description);
        removeChildrenFrom(content);
    }

    private void showResource(Node node) {
        if (node.address.isEmpty()) {
            header.add(code().textContent(node.address.toString()));
        } else {
            Segment last = node.address.last();
            header.add(code().style("overflow-wrap", "break-word").textContent(last.key + "=" + last.value));
        }

        Operation operation = new Operation.Builder(node.address.resolve(uic.statementContext), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Task<FlowContext> readResource = context -> uic.dispatcher.execute(operation).then(result -> {
            context.set("result", result);
            return context.resolve();
        });

        Task<FlowContext> lookupMetadata = context -> uic.metadataLookup.lookup(node.address).then(metadata -> {
            context.set("metadata", metadata);
            return context.resolve();
        });

        Flow.parallel(new FlowContext(), asList(readResource, lookupMetadata)).subscribe(context -> {
            if (context.successful()) {
                Metadata metadata = context.get("metadata");
                description.textContent(metadata.resourceDescription.description());

                ModelNode result = context.get("result");
                content.add(descriptionList()
                        .horizontal()
                        .horizontalTermWidth(breakpoints(
                                default_, "12ch",
                                sm, "15ch",
                                md, "20ch",
                                lg, "28ch",
                                xl, "30ch",
                                _2xl, "35ch"))
                        .run(dl -> {
                            List<Property> properties = result.asPropertyList();
                            properties.sort(comparing(Property::getName));
                            for (Property property : properties) {
                                if (property.getValue().getType() != ModelType.OBJECT) {
                                    dl.addGroup(descriptionListGroup()
                                            .addTerm(descriptionListTerm(property.getName()))
                                            .addDescription(descriptionListDescription(property.getValue().asString())));
                                }
                            }
                        }));
            } else {
                // TODO Error handling
            }
        });
    }
}
