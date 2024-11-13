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
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.FormSelect;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.inputgroup.InputGroup;

import elemental2.dom.HTMLElement;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MIN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.resource.FormItem.InputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.FormItem.InputMode.NATIVE;
import static org.jboss.hal.ui.resource.HelperTexts.notNumeric;
import static org.jboss.hal.ui.resource.HelperTexts.required;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormSelect.formSelect;
import static org.patternfly.component.form.FormSelectOption.formSelectOption;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.form.TextInputType.number;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;

// TODO Implement sensitive
//  Example: /subsystem=transactions, attribute "process-id-socket-max-ports"
class NumberFormItem extends FormItem {

    /**
     * @see <a
     * href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MIN_SAFE_INTEGER">Number.MIN_SAFE_INTEGER</a>
     */
    private static final long MIN_SAFE_LONG = -9007199254740991L;

    /**
     * @see <a
     * href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_SAFE_INTEGER">Number.MAX_SAFE_INTEGER</a>
     */
    private static final long MAX_SAFE_LONG = 9007199254740991L;

    private FormSelect allowedValuesControl;
    private TextInput minMaxControl;

    NumberFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label) {
        super(identifier, ra, label);
        defaultSetup();
    }

    FormGroupControl readOnlyGroup() {
        TextInput textControl = textControl().readonly();
        if (ra.expression) {
            return formGroupControl()
                    .addInputGroup(inputGroup()
                            .addItem(inputGroupItem().fill().addControl(textControl))
                            .addItem(inputGroupItem().addButton(resolveExpressionButton()))
                            .run(ig -> {
                                if (ra.description.unit() != null) {
                                    ig.addText(unitInputGroupText());
                                }
                            }));
        } else {
            if (ra.description.unit() != null) {
                return formGroupControl()
                        .addInputGroup(inputGroup()
                                .addItem(inputGroupItem().fill().addControl(textControl))
                                .addText(unitInputGroupText()));
            } else {
                return formGroupControl().addControl(textControl);
            }
        }
    }

    FormGroupControl nativeGroup() {
        if (ra.description.hasDefined(ALLOWED)) {
            return formGroupControl().addControl(allowedValuesControl());
        } else {
            return formGroupControl().addControl(minMaxControl());
        }
    }

    HTMLElement nativeContainer() {
        InputGroup inputGroup = inputGroup().addItem(inputGroupItem().addButton(switchToExpressionModeButton()));
        if (ra.description.hasDefined(ALLOWED)) {
            inputGroup.addItem(inputGroupItem().fill().addControl(allowedValuesControl()));
        } else {
            inputGroup.addItem(inputGroupItem().fill().addControl(minMaxControl()));
        }
        if (ra.description.unit() != null) {
            inputGroup.addText(unitInputGroupText());
        }
        return inputGroup.element();
    }

    private FormSelect allowedValuesControl() {
        List<Long> allowedValues = ra.description.get(ALLOWED).asList().stream().map(ModelNode::asLong).collect(toList());
        allowedValuesControl = formSelect(identifier)
                .run(fs -> {
                    if (ra.description.nillable()) {
                        fs.addOption(formSelectOption(UNDEFINED));
                    }
                })
                .addOptions(allowedValues, n -> formSelectOption(String.valueOf(n)))
                .run(fs -> {
                    if (ra.value.isDefined()) {
                        fs.value(ra.value.asString());
                    } else if (ra.description.nillable()) {
                        fs.value(UNDEFINED);
                    }
                });
        return allowedValuesControl;
    }

    private TextInput minMaxControl() {
        minMaxControl = textInput(number, identifier)
                .run(ti -> {
                    if (ra.value.isDefined()) {
                        ti.value(ra.value.asString());
                    } else {
                        ti.placeholder("undefined");
                    }
                });
        ModelType type = ra.description.get(TYPE).asType();
        if (type == ModelType.INT) {
            int min = max(ra.description.get(MIN).asInt(Integer.MIN_VALUE), Integer.MIN_VALUE);
            int max = min(ra.description.get(MAX).asInt(Integer.MAX_VALUE), Integer.MAX_VALUE);
            minMaxControl.inputElement().min(min).max(max).apply(e -> e.step = "1");
        } else if (type == ModelType.LONG) {
            String min = String.valueOf(max(ra.description.get(MIN).asLong(MIN_SAFE_LONG), MIN_SAFE_LONG));
            String max = String.valueOf(min(ra.description.get(MAX).asLong(MAX_SAFE_LONG), MAX_SAFE_LONG));
            minMaxControl.inputElement().min(min).max(max).apply(e -> e.step = "1");
        } else if (type == ModelType.DOUBLE) {
            minMaxControl.inputElement().apply(e -> e.step = "any");
        }
        return minMaxControl;
    }

    // ------------------------------------------------------ validation

    @Override
    void resetValidation() {
        super.resetValidation();
        if (allowedValuesControl != null) {
            allowedValuesControl.resetValidation();
        }
        if (minMaxControl != null) {
            minMaxControl.resetValidation();
        }
    }

    @Override
    boolean validate() {
        if (inputMode == NATIVE) {
            if (allowedValuesControl != null) {
                if (requiredOnItsOwn() && UNDEFINED.equals(allowedValuesControl.value())) {
                    allowedValuesControl.validated(error);
                    formGroupControl.addHelperText(required(ra));
                    return false;
                }
            } else if (minMaxControl != null) {
                if (requiredOnItsOwn() && minMaxControl.value().isEmpty()) {
                    minMaxControl.validated(error);
                    formGroupControl.addHelperText(required(ra));
                    return false;
                } else if (!isNumeric(minMaxControl.value())) {
                    minMaxControl.validated(error);
                    formGroupControl.addHelperText(notNumeric(ra));
                }
            }
        } else if (inputMode == EXPRESSION) {
            return validateExpressionMode();
        }
        return true;
    }

    private boolean isNumeric(String value) {
        ModelType type = ra.description.get(TYPE).asType();
        if (type == ModelType.INT) {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        } else if (type == ModelType.LONG) {
            try {
                Long.parseLong(value);
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        } else if (type == ModelType.DOUBLE) {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException ignored) {
                return false;
            }
        } else {
            return false;
        }
    }

    // ------------------------------------------------------ data

    @Override
    boolean isModified() {
        String originalValue = ra.value.asString();
        boolean wasDefined = ra.value.isDefined();

        if (inputMode == NATIVE) {
            if (allowedValuesControl != null) {
                String selectedValue = allowedValuesControl.value();
                if (wasDefined) {
                    // modified if the original value was an expression or is different from the current user input
                    return ra.expression || !originalValue.equals(selectedValue);
                } else {
                    return !UNDEFINED.equals(selectedValue);
                }
            } else if (minMaxControl != null) {
                if (wasDefined) {
                    // modified if the original value was an expression or is different from the current user input
                    return ra.expression || !originalValue.equals(minMaxControl.value());
                } else {
                    return !minMaxControl.value().isEmpty();
                }
            }
        } else if (inputMode == EXPRESSION) {
            if (wasDefined) {
                return !originalValue.equals(textControlValue());
            } else {
                return !textControlValue().isEmpty();
            }
        }
        return false;
    }

    @Override
    ModelNode modelNode() {
        if (inputMode == NATIVE) {
            if (allowedValuesControl != null) {
                String selectedValue = allowedValuesControl.value();
                if (UNDEFINED.equals(selectedValue)) {
                    return new ModelNode();
                } else {
                    return new ModelNode().set(numericModelNode(selectedValue));
                }
            } else if (minMaxControl != null) {
                return numericModelNode(minMaxControl.value());
            }
        } else if (inputMode == EXPRESSION) {
            return expressionModelNode();
        }
        return new ModelNode();
    }

    private ModelNode numericModelNode(String value) {
        ModelType type = ra.description.get(TYPE).asType();
        if (type == ModelType.INT) {
            return new ModelNode().set(Integer.parseInt(value));
        } else if (type == ModelType.LONG) {
            return new ModelNode().set(Long.parseLong(value));
        } else if (type == ModelType.DOUBLE) {
            return new ModelNode().set(Double.parseDouble(value));
        } else {
            return new ModelNode();
        }
    }
}
