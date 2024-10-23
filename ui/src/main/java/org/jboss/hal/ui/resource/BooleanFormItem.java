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

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.hal.meta.Metadata;
import org.patternfly.component.button.Button;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.small;
import static org.jboss.hal.resources.HalClasses.edit;
import static org.jboss.hal.resources.HalClasses.expression;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resourceManager;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.switch_.Switch.switch_;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.icon.IconSets.fas.link;
import static org.patternfly.icon.IconSets.fas.toggleOn;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.Spacer.md;
import static org.patternfly.style.Classes.util;

public class BooleanFormItem extends FormItem {

    private final String switchToExpressionModeId;
    private final String switchToToggleModeId;
    private final String resolveExpressionId;
    private final FormGroupControl control;
    private HTMLElement expressionMode;
    private HTMLElement toggleMode;

    BooleanFormItem(String identifier, Metadata metadata, ResourceAttribute ra, FormGroupLabel label) {
        super(identifier);
        switchToExpressionModeId = Id.unique();
        switchToToggleModeId = Id.unique();
        resolveExpressionId = Id.unique();

        if (ra.description.expressionAllowed()) {
            if (ra.description.readOnly()) {

            }

            Button switchToExpressionMode = button().id(switchToExpressionModeId).css(util("ml-md")).plain().icon(link())
                    .onClick((e, b) -> switchToExpressionMode());
            Button switchToToggleMode = button().id(switchToToggleModeId).control().icon(toggleOn())
                    .onClick((e, b) -> switchToToggleMode());
            Button resolveExpression = button().id(resolveExpressionId).control().icon(link())
                    .onClick((e, b) -> resolveExpression());

            expressionMode = inputGroup()
                    .addItem(inputGroupItem()
                            .addButton(switchToToggleMode))
                    .addItem(inputGroupItem().fill()
                            .addFormControl(textInput(identifier)
                                    .value(ra.value.asString())))
                    .addItem(inputGroupItem()
                            .addButton(resolveExpression))
                    .element();

            toggleMode = flex().alignItems(center)
                    .css(halComponent(resourceManager, edit, expression, Classes.switch_))
                    .addItem(flexItem().spacer(md)
                            .add(small()
                                    .add(switchToExpressionMode.inline().link())))
                    .addItem(flexItem()
                            .add(switch_(identifier, identifier, ra.booleanValue())
                                    .checkIcon()
                                    .ariaLabel(ra.name)
                                    .readonly(ra.description.readOnly())))
                    .element();

            control = formGroupControl();
            if (ra.expression) {
                switchToExpressionMode();
            } else {
                switchToToggleMode();
            }

        } else {
            control = formGroupControl()
                    .add(switch_(identifier, identifier, ra.booleanValue())
                            .checkIcon()
                            .ariaLabel(ra.name)
                            .readonly(ra.description.readOnly()));
        }

        formGroup = formGroup(identifier)
                .required(ra.description.required())
                .addLabel(label)
                .addControl(control);
    }

    private void switchToExpressionMode() {
        failSafeRemoveFromParent(toggleMode);
        control.add(expressionMode);
        tooltip(By.id(switchToToggleModeId), "Switch to toggle mode").appendTo(expressionMode);
        tooltip(By.id(resolveExpressionId), "Resolve expression").appendTo(expressionMode);

    }

    private void switchToToggleMode() {
        failSafeRemoveFromParent(expressionMode);
        control.add(toggleMode);
        tooltip(By.id(switchToExpressionModeId), "Switch to expression mode").appendTo(toggleMode);
    }

    private void resolveExpression() {
        // TODO Resolve expression
    }
}
