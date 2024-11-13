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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.BuildingBlocks;
import org.patternfly.component.button.Button;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.switch_.Switch;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.resources.HalClasses.edit;
import static org.jboss.hal.resources.HalClasses.expression;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resourceManager;
import static org.jboss.hal.ui.resource.FormItem.InputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.FormItem.InputMode.NATIVE;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.switch_.Switch.switch_;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.none;
import static org.patternfly.style.Classes.switch_;

// TODO Implement sensitive
//  Example: /subsystem=jmx, attribute "non-core-mbean-sensitivity"
public class BooleanFormItem extends FormItem {

    // The select control is created by selectControl() called during defaultSetup().
    // It's, so to speak, final and never null!
    private /*final*/ Switch switchControl;

    BooleanFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label) {
        super(identifier, ra, label);
        defaultSetup();
    }

    FormGroupControl readOnlyGroup() {
        TextInput textControl = textControl().readonly();
        if (ra.expression) {
            return formGroupControl()
                    .addInputGroup(inputGroup()
                            .addItem(inputGroupItem().fill().addControl(textControl))
                            .addItem(inputGroupItem().addButton(resolveExpressionButton())));
        } else if (!ra.value.isDefined()) {
            return formGroupControl().addControl(textControl);
        } else {
            return formGroupControl().add(switchControl());
        }
    }

    FormGroupControl nativeGroup() {
        return formGroupControl().add(switchControl());
    }

    HTMLElement nativeContainer() {
        return flex().alignItems(center).spaceItems(none)
                .css(halComponent(resourceManager, edit, expression, switch_))
                .addItem(flexItem().add(switchToExpressionModeButton()))
                .addItem(flexItem().add(switchControl()))
                .element();
    }

    @Override
    Button switchToExpressionModeButton() {
        return button().id(switchToExpressionModeId).plain().icon(BuildingBlocks.expressionMode().get())
                .onClick((e, b) -> switchToExpressionMode());
    }

    private Switch switchControl() {
        switchControl = switch_(identifier, identifier, ra.booleanValue())
                .checkIcon()
                .ariaLabel(ra.name)
                .readonly(ra.description.readOnly());
        return switchControl;
    }

    // ------------------------------------------------------ validation

    @Override
    boolean validate() {
        if (inputMode == InputMode.EXPRESSION) {
            return validateExpressionMode();
        }
        return true;
    }

    // ------------------------------------------------------ data

    @Override
    boolean isModified() {
        boolean originalValue = ra.value.asBoolean();
        boolean wasDefined = ra.value.isDefined();

        if (inputMode == NATIVE) {
            if (wasDefined) {
                // modified if the original value was an expression or is different from the current user input
                return ra.expression || originalValue != switchControl.value();
            } else {
                return true;
            }
        } else if (inputMode == EXPRESSION) {
            return isExpressionModified();
        }
        return false;
    }

    @Override
    ModelNode modelNode() {
        if (inputMode == NATIVE) {
            return new ModelNode().set(switchControl.value());
        } else if (inputMode == EXPRESSION) {
            return expressionModelNode();
        }
        return new ModelNode();
    }
}
