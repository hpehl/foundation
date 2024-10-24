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

import org.patternfly.component.form.FormGroupLabel;

import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.TextInput.textInput;

class FallbackFormItem extends FormItem {

    FallbackFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label) {
        super(identifier);
        formGroup = formGroup(identifier)
                .required(ra.description.required())
                .addLabel(label)
                .addControl(formGroupControl()
                        .addControl(textInput(identifier)
                                .readonly() // the fallback form item is always read-only!
                                .run(ti -> {
                                    if (ra.value.isDefined()) {
                                        ti.value(ra.value.asString());
                                    } else {
                                        ti.placeholder("undefined");
                                    }
                                })));
    }
}
