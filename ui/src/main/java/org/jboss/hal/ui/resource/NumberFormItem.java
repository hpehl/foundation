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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.FormSelect;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.form.TextInputType;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MIN;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormSelect.formSelect;
import static org.patternfly.component.form.FormSelectOption.formSelectOption;
import static org.patternfly.component.form.TextInput.textInput;

class NumberFormItem extends FormItem {

    /**
     * @see <a
     * href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MIN_SAFE_INTEGER">Number.MIN_SAFE_INTEGER</a>
     */
    public static final long MIN_SAFE_LONG = -9007199254740991L;

    /**
     * @see <a
     * href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_SAFE_INTEGER">Number.MAX_SAFE_INTEGER</a>
     */
    public static final long MAX_SAFE_LONG = 9007199254740991L;

    NumberFormItem(String identifier, ModelType type, ResourceAttribute ra, FormGroupLabel label) {
        super(identifier);
        FormGroupControl control;
        List<Long> allowedValues = allowedValues(ra.description);

        if (!allowedValues.isEmpty()) {
            FormSelect formSelect = formSelect(identifier);
            control = formGroupControl()
                    .addControl(formSelect
                            .addOptions(allowedValues, n -> formSelectOption(String.valueOf(n))));
            formSelect.value(ra.value.asString());

        } else {
            long min, max;
            if (type == ModelType.INT) {
                min = max(ra.description.get(MIN).asLong(Integer.MIN_VALUE), Integer.MIN_VALUE);
                max = min(ra.description.get(MAX).asLong(Integer.MAX_VALUE), Integer.MAX_VALUE);
            } else {
                min = max(ra.description.get(MIN).asLong(MIN_SAFE_LONG), MIN_SAFE_LONG);
                max = min(ra.description.get(MAX).asLong(MAX_SAFE_LONG), MAX_SAFE_LONG);
            }
            TextInput textInput = textInput(TextInputType.number, identifier)
                    .value(ra.value.asString())
                    .applyTo(input -> {
                        input.min(String.valueOf(min));
                        input.max(String.valueOf(max));
                        input.readOnly(ra.description.readOnly());
                    });
            control = formGroupControl()
                    .addControl(textInput);
        }

        formGroup = formGroup(identifier)
                .required(ra.description.required())
                .addLabel(label)
                .addControl(control);
    }

    private List<Long> allowedValues(AttributeDescription description) {
        if (description.hasDefined(ALLOWED)) {
            return description.get(ALLOWED).asList().stream().map(ModelNode::asLong).collect(toList());
        }
        return emptyList();
    }
}
