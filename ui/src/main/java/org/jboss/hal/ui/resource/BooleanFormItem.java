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
import org.patternfly.component.button.Button;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
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
import static org.patternfly.icon.IconSets.fas.dollarSign;
import static org.patternfly.icon.IconSets.fas.link;
import static org.patternfly.icon.IconSets.fas.terminal;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.none;

// TODO Can a boolean value be sensitive?
public class BooleanFormItem extends FormItem {

    private final String switchToExpressionModeId;
    private final String switchToNormalModeId;
    private final String resolveExpressionId;
    private final FormGroupControl control;
    private HTMLElement expressionMode;
    private HTMLElement toggleMode;

    BooleanFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label) {
        super(identifier);
        switchToExpressionModeId = Id.build(identifier, "switch-to-expression-mode");
        switchToNormalModeId = Id.build(identifier, "switch-to-normal-mode");
        resolveExpressionId = Id.build(identifier, "resolve-expression");

        if (ra.description.readOnly()) {
            control = readOnlyGroup(identifier, ra);
        } else {
            if (ra.description.expressionAllowed()) {
                expressionMode = expressionMode(identifier, ra);
                toggleMode = normalMode(identifier, ra);
                control = formGroupControl();
                if (ra.expression) {
                    switchToExpressionMode();
                } else {
                    switchToNormalMode();
                }
            } else {
                control = switchGroup(identifier, ra);
            }
        }
        formGroup = formGroup(identifier)
                .required(ra.description.required())
                .addLabel(label)
                .addControl(control);
    }

    // ------------------------------------------------------ building blocks

    private FormGroupControl readOnlyGroup(String identifier, ResourceAttribute ra) {
        if (ra.expression) {
            return formGroupControl()
                    .addInputGroup(inputGroup()
                            .addItem(inputGroupItem().fill()
                                    .addControl(textInput(identifier)
                                            .readonly()
                                            .value(ra.value.asString())))
                            .addItem(inputGroupItem()
                                    .addButton(resolveExpressionButton(ra))))
                    .add(tooltip(By.id(resolveExpressionId), "Resolve expression"));
        } else if (!ra.value.isDefined()) {
            return formGroupControl()
                    .addControl(textInput(identifier)
                            .readonly()
                            .placeholder("undefined"));
        } else {
            return formGroupControl()
                    .add(switch_(identifier, identifier)
                            .readonly()
                            .value(ra.value.asBoolean())
                            .ariaLabel(ra.name)
                            .checkIcon());
        }
    }

    private HTMLElement expressionMode(String identifier, ResourceAttribute ra) {
        Button switchToNormalMode = button().id(switchToNormalModeId).control().icon(terminal())
                .onClick((e, b) -> switchToNormalMode());
        return inputGroup()
                .addItem(inputGroupItem()
                        .addButton(switchToNormalMode))
                .addItem(inputGroupItem().fill()
                        .addControl(textInput(identifier)
                                .run(ti -> {
                                    if (ra.value.isDefined()) {
                                        ti.value(ra.value.asString());
                                    } else {
                                        ti.placeholder("undefined");
                                    }
                                })))
                .addItem(inputGroupItem()
                        .addButton(resolveExpressionButton(ra)))
                .element();
    }

    private HTMLElement normalMode(String identifier, ResourceAttribute ra) {
        Button switchToExpressionMode = button().id(switchToExpressionModeId).plain().icon(dollarSign())
                .onClick((e, b) -> switchToExpressionMode());
        return flex().alignItems(center).spaceItems(none)
                .css(halComponent(resourceManager, edit, expression, Classes.switch_))
                .addItem(flexItem()
                        .add(switchToExpressionMode))
                .addItem(flexItem()
                        .add(switch_(identifier, identifier, ra.booleanValue())
                                .checkIcon()
                                .ariaLabel(ra.name)
                                .readonly(ra.description.readOnly())))
                .element();
    }

    private FormGroupControl switchGroup(String identifier, ResourceAttribute ra) {
        return formGroupControl()
                .add(switch_(identifier, identifier, ra.booleanValue())
                        .checkIcon()
                        .ariaLabel(ra.name));
    }

    private Button resolveExpressionButton(ResourceAttribute ra) {
        return button().id(resolveExpressionId).control().icon(link())
                .onClick((e, b) -> resolveExpression(ra.value.asString()));
    }

    // ------------------------------------------------------ event handlers

    private void switchToExpressionMode() {
        failSafeRemoveFromParent(toggleMode);
        control.add(expressionMode);
        tooltip(By.id(switchToNormalModeId), "Switch to normal mode").appendTo(expressionMode);
        tooltip(By.id(resolveExpressionId), "Resolve expression").appendTo(expressionMode);

    }

    private void switchToNormalMode() {
        failSafeRemoveFromParent(expressionMode);
        control.add(toggleMode);
        tooltip(By.id(switchToExpressionModeId), "Switch to expression mode").appendTo(toggleMode);
    }
}
