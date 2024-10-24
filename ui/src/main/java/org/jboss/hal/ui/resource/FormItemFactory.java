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
package org.jboss.hal.ui.resource;

import org.jboss.elemento.ButtonType;
import org.jboss.elemento.Elements;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.Keys;
import org.jboss.hal.ui.LabelBuilder;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.core.Aria;
import org.patternfly.icon.IconSets;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.insertFirst;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.resources.HalClasses.deprecated;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.resources.HalClasses.resourceManager;
import static org.jboss.hal.resources.HalClasses.stabilityLevel;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescriptionPopover;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.ResourceManager.State.EDIT;
import static org.patternfly.component.form.FormGroupLabel.formGroupLabel;
import static org.patternfly.style.Classes.component;
import static org.patternfly.style.Classes.form;
import static org.patternfly.style.Classes.group;
import static org.patternfly.style.Classes.help;
import static org.patternfly.style.Classes.label;
import static org.patternfly.style.Classes.text;
import static org.patternfly.style.Classes.util;

class FormItemFactory {

    private static final Logger logger = Logger.getLogger(FormItemFactory.class.getName());

    static FormItem formItem(AddressTemplate template, Metadata metadata, ResourceAttribute ra) {
        FormItem formItem;
        String identifier = ItemIdentifier.identifier(ra, EDIT);
        FormGroupLabel formGroupLabel = label(metadata, ra, identifier);

        if (!ra.readable) {
            formItem = new RestrictedFormItem(identifier, ra, formGroupLabel);
        } else {
            if (ra.description.hasDefined(TYPE)) {
                ModelType type = ra.description.get(TYPE).asType();
                switch (type) {
                    case BOOLEAN:
                        formItem = new BooleanFormItem(identifier, ra, formGroupLabel);
                        break;

                    case BIG_INTEGER:
                    case INT:
                    case LONG:
                        formItem = new NumberFormItem(identifier, ra, formGroupLabel);
                        break;

                    case DOUBLE:
                        formItem = new FallbackFormItem(identifier, ra, formGroupLabel);
                        break;

                    case STRING:
                        formItem = new FallbackFormItem(identifier, ra, formGroupLabel);

                    case LIST:
                        formItem = new FallbackFormItem(identifier, ra, formGroupLabel);
                        break;

                    case OBJECT:
                        formItem = new FallbackFormItem(identifier, ra, formGroupLabel);
                        break;

                    // unsupported types
                    case BIG_DECIMAL:
                    case BYTES:
                    case EXPRESSION:
                    case PROPERTY:
                    case TYPE:
                    case UNDEFINED:
                        formItem = new FallbackFormItem(identifier, ra, formGroupLabel);
                        logger.warn("Unsupported type %s for attribute %s in resource %s. " +
                                "Unable to create a form item. Attribute will be skipped.", type.name(), ra.name, template);
                        break;

                    default:
                        formItem = new FallbackFormItem(identifier, ra, formGroupLabel);
                        break;
                }
            } else {
                formItem = new FallbackFormItem(identifier, ra, formGroupLabel);
            }
        }
        return formItem.store(Keys.RESOURCE_ATTRIBUTE, ra);
    }

    private static FormGroupLabel label(Metadata metadata, ResourceAttribute ra, String identifier) {
        FormGroupLabel formGroupLabel;
        LabelBuilder labelBuilder = new LabelBuilder();
        if (ra.description != null) {
            if (ra.description.nested()) {
                // <unstable>
                // If the internal DOM of FormGroupLabel changes, this will no longer work
                AttributeDescription parentDescription = ra.description.parent();
                AttributeDescription nestedDescription = ra.description;
                String parentLabel = labelBuilder.label(parentDescription.name());
                String nestedLabel = labelBuilder.label(ra.name);
                formGroupLabel = formGroupLabel(nestedLabel)
                        .css(halComponent(resourceManager, HalClasses.nestedLabel))
                        .help(nestedLabel + " description", attributeDescriptionPopover(nestedLabel, nestedDescription));
                HTMLElement parentLabelElement = Elements.label().css(component(form, label))
                        .apply(l -> l.htmlFor = identifier)
                        .add(span().css(component(form, label, text))
                                .textContent(parentLabel))
                        .element();
                HTMLButtonElement parentHelpButton = Elements.button(ButtonType.button)
                        .css(component(form, group, label, help))
                        .aria(Aria.label, parentLabel + " description")
                        .add(IconSets.patternfly.help())
                        .element();
                // Use insert-first calls and add the elements in reverse order
                // to not mess with the required marker added in FormGroupLabel.attach()
                insertFirst(formGroupLabel.element(), span().textContent("/"));
                insertFirst(formGroupLabel.element(), parentHelpButton);
                insertFirst(formGroupLabel.element(), parentLabelElement);
                attributeDescriptionPopover(parentLabel, parentDescription)
                        .trigger(parentHelpButton)
                        .appendToBody();
                // </unstable>
            } else {
                String label = labelBuilder.label(ra.name);
                formGroupLabel = formGroupLabel(label)
                        .help(label + " description", attributeDescriptionPopover(label, ra.description));

                // only the top level attribute is stability-labeled
                if (uic().environment()
                        .highlightStability(metadata.resourceDescription().stability(), ra.description.stability())) {
                    formGroupLabel.css(halComponent(resourceManager, stabilityLevel))
                            .add(stabilityLabel(ra.description.stability()).compact()
                                    .style("align-self", "baseline")
                                    .css(util("ml-sm"), util("font-weight-normal"))
                                    .element());
                }
            }
            if (ra.description.deprecation().isDefined()) {
                formGroupLabel.classList().add(halModifier(deprecated));
            }
        } else {
            formGroupLabel = formGroupLabel(labelBuilder.label(ra.name));
        }
        return formGroupLabel;
    }
}
