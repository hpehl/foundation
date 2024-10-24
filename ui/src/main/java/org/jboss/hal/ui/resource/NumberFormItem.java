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

import org.jboss.elemento.By;
import org.jboss.elemento.Id;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.patternfly.component.button.Button;
import org.patternfly.component.form.FormControl;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.inputgroup.InputGroup;
import org.patternfly.component.inputgroup.InputGroupText;

import elemental2.dom.HTMLElement;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.small;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MIN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormSelect.formSelect;
import static org.patternfly.component.form.FormSelectOption.formSelectOption;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.form.TextInputType.number;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.inputgroup.InputGroupText.inputGroupText;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.icon.IconSets.fas.dollarSign;
import static org.patternfly.icon.IconSets.fas.link;
import static org.patternfly.icon.IconSets.fas.terminal;

// TODO Can a numeric value be sensitive?
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

    private final String switchToExpressionModeId;
    private final String switchToNormalModeId;
    private final String resolveExpressionId;
    private final FormGroupControl control;
    private HTMLElement expressionMode;
    private HTMLElement toggleMode;

    NumberFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label) {
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
                control = numberInputGroup(identifier, ra);
            }
        }
        formGroup = formGroup(identifier)
                .required(ra.description.required())
                .addLabel(label)
                .addControl(control);
    }

    // ------------------------------------------------------ building blocks

    private FormGroupControl readOnlyGroup(String identifier, ResourceAttribute ra) {
        String unit = ra.description.unit();
        if (ra.expression) {
            return formGroupControl()
                    .addInputGroup(inputGroup()
                            .addItem(inputGroupItem().fill()
                                    .addControl(textInput(identifier)
                                            .readonly()
                                            .value(ra.value.asString())))
                            .addItem(inputGroupItem()
                                    .addButton(resolveExpressionButton(ra)))
                            .run(ig -> {
                                if (unit != null) {
                                    ig.addText(unit(unit));
                                }
                            }))
                    .add(tooltip(By.id(resolveExpressionId), "Resolve expression"));
        } else {
            TextInput textInput = textInput(identifier).readonly();
            if (ra.value.isDefined()) {
                textInput.value(ra.value.asString());

            } else {
                textInput.placeholder("undefined");
            }
            if (unit != null) {
                return formGroupControl()
                        .addInputGroup(inputGroup()
                                .addItem(inputGroupItem().fill()
                                        .addControl(textInput))
                                .addText(unit(unit)));
            } else {
                return formGroupControl()
                        .addControl(textInput);
            }
        }
    }

    private HTMLElement expressionMode(String identifier, ResourceAttribute ra) {
        String unit = ra.description.unit();
        Button switchToNormalMode = button().id(switchToNormalModeId).control().icon(terminal())
                .onClick((e, b) -> switchToNormalMode());
        InputGroup inputGroup = inputGroup()
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
                        .addButton(resolveExpressionButton(ra)));
        if (unit != null) {
            inputGroup.addText(unit(unit));
        }
        return inputGroup.element();
    }

    private HTMLElement normalMode(String identifier, ResourceAttribute ra) {
        String unit = ra.description.unit();
        Button switchToExpressionMode = button().id(switchToExpressionModeId).control().icon(dollarSign())
                .onClick((e, b) -> switchToExpressionMode());
        InputGroup inputGroup = inputGroup()
                .addItem(inputGroupItem()
                        .addButton(switchToExpressionMode));
        if (ra.description.hasDefined(ALLOWED)) {
            inputGroup.addItem(inputGroupItem().fill()
                    .addControl(allowedValuesControl(identifier, ra)));
        } else {
            inputGroup.addItem(inputGroupItem().fill()
                    .addControl(minMaxControl(identifier, ra)));
        }
        if (unit != null) {
            inputGroup.addText(unit(unit));
        }
        return inputGroup.element();
    }

    private FormGroupControl numberInputGroup(String identifier, ResourceAttribute ra) {
        if (ra.description.hasDefined(ALLOWED)) {
            return formGroupControl()
                    .addControl(allowedValuesControl(identifier, ra));
        } else {
            return formGroupControl()
                    .addControl(minMaxControl(identifier, ra));
        }
    }

    private FormControl<?, ?> allowedValuesControl(String identifier, ResourceAttribute ra) {
        List<Long> allowedValues = ra.description.get(ALLOWED).asList().stream().map(ModelNode::asLong).collect(toList());
        return formSelect(identifier)
                .run(fs -> {
                    if (ra.description.nillable()) {
                        fs.addOption(formSelectOption("undefined"));
                    }
                })
                .addOptions(allowedValues, n -> formSelectOption(String.valueOf(n)))
                .run(fs -> {
                    if (ra.value.isDefined()) {
                        fs.value(ra.value.asString());
                    } else if (ra.description.nillable()) {
                        fs.value("undefined");
                    }
                });
    }

    private FormControl<?, ?> minMaxControl(String identifier, ResourceAttribute ra) {
        long min, max;
        ModelType type = ra.description.get(TYPE).asType();
        if (type == ModelType.INT) {
            min = max(ra.description.get(MIN).asLong(Integer.MIN_VALUE), Integer.MIN_VALUE);
            max = min(ra.description.get(MAX).asLong(Integer.MAX_VALUE), Integer.MAX_VALUE);
        } else {
            min = max(ra.description.get(MIN).asLong(MIN_SAFE_LONG), MIN_SAFE_LONG);
            max = min(ra.description.get(MAX).asLong(MAX_SAFE_LONG), MAX_SAFE_LONG);
        }
        return textInput(number, identifier)
                .run(ti -> {
                    if (ra.value.isDefined()) {
                        ti.value(ra.value.asString());
                    } else {
                        ti.placeholder("undefined");
                    }
                })
                .applyTo(input -> {
                    input.min(String.valueOf(min));
                    input.max(String.valueOf(max));
                });
    }

    private Button resolveExpressionButton(ResourceAttribute ra) {
        return button().id(resolveExpressionId).control().icon(link())
                .onClick((e, b) -> resolveExpression(ra.value.asString()));
    }

    private InputGroupText unit(String unit) {
        return inputGroupText().plain().add(small().textContent(unit));
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
