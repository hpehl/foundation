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

import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.TextInput;

import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;

class StringFormItem extends FormItem {

    StringFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label) {
        super(identifier, ra, label);

        if (ra.description.readOnly()) {
            formGroupControl = readOnlyGroup();
        } else {
            if (ra.description.expressionAllowed()) {
                formGroupControl = expressionGroup();
                toggleResolveExpression(ra.value.asString());
            } else {
                formGroupControl = normalGroup();
            }
        }
        formGroup = formGroup(identifier)
                .required(ra.description.required())
                .addLabel(label)
                .addControl(formGroupControl);
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
        } else {
            return formGroupControl()
                    .addControl(textControl);
        }
    }

    private FormGroupControl expressionGroup() {
        return formGroupControl()
                .addInputGroup(inputGroup()
                        .addItem(inputGroupItem().fill()
                                .addControl(textControl()
                                        .onChange((event, component, value) -> toggleResolveExpression(value))))
                        .addItem(resolveExpressionItem = inputGroupItem()
                                .addButton(resolveExpressionButton())));
    }

    private FormGroupControl normalGroup() {
        return formGroupControl()
                .addControl(textControl());
    }
}
