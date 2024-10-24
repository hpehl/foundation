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

import org.patternfly.component.button.Button;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.switch_.Switch;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.resources.HalClasses.edit;
import static org.jboss.hal.resources.HalClasses.expression;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resourceManager;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.switch_.Switch.switch_;
import static org.patternfly.icon.IconSets.fas.dollarSign;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.none;

// TODO Can a boolean value be sensitive?
public class BooleanFormItem extends FormItem {

    BooleanFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label) {
        super(identifier, ra, label);
        defaultSetup();
    }

    FormGroupControl readOnlyGroup() {
        TextInput textControl = textControl().readonly();
        if (ra.expression) {
            return formGroupControl()
                    .addInputGroup(inputGroup()
                            .addItem(inputGroupItem().fill()
                                    .addControl(textControl))
                            .addItem(inputGroupItem()
                                    .addButton(resolveExpressionButton())));
        } else if (!ra.value.isDefined()) {
            return formGroupControl()
                    .addControl(textControl);
        } else {
            return formGroupControl()
                    .add(switchControl());
        }
    }

    FormGroupControl nativeGroup() {
        return formGroupControl()
                .add(switchControl());
    }

    HTMLElement normalMode() {
        return flex().alignItems(center).spaceItems(none)
                .css(halComponent(resourceManager, edit, expression, Classes.switch_))
                .addItem(flexItem()
                        .add(switchToExpressionModeButton()))
                .addItem(flexItem()
                        .add(switchControl()))
                .element();
    }

    @Override
    Button switchToExpressionModeButton() {
        return button().id(switchToExpressionModeId).plain().icon(dollarSign())
                .onClick((e, b) -> switchToExpressionMode());
    }

    private Switch switchControl() {
        return switch_(identifier, identifier, ra.booleanValue())
                .checkIcon()
                .ariaLabel(ra.name)
                .readonly(ra.description.readOnly());
    }
}
