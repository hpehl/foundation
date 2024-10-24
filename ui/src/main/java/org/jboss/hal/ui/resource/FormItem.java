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

import java.util.HashMap;
import java.util.Map;

import org.jboss.elemento.By;
import org.jboss.elemento.HasElement;
import org.jboss.elemento.Id;
import org.jboss.elemento.logger.Logger;
import org.patternfly.component.WithIdentifier;
import org.patternfly.component.button.Button;
import org.patternfly.component.form.FormGroup;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.inputgroup.InputGroupItem;
import org.patternfly.component.inputgroup.InputGroupText;
import org.patternfly.core.ComponentContext;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelNodeHelper.isExpression;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.component.inputgroup.InputGroupText.inputGroupText;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.icon.IconSets.fas.dollarSign;
import static org.patternfly.icon.IconSets.fas.link;
import static org.patternfly.icon.IconSets.fas.terminal;

/** An item for a {@link ResourceForm} based on a {@link FormGroup} */
abstract class FormItem implements
        ManagerItem<FormItem>,
        HasElement<HTMLElement, FormItem>,
        ComponentContext<HTMLElement, FormItem>,
        WithIdentifier<HTMLElement, FormItem> {

    private static final Logger logger = Logger.getLogger(FormItem.class.getName());

    final String identifier;
    final ResourceAttribute ra;
    final String switchToExpressionModeId;
    private final String switchToNormalModeId;
    private final String resolveExpressionId;
    private final Map<String, Object> data;
    private final FormGroupLabel label;

    FormGroup formGroup;
    FormGroupControl formGroupControl;
    InputGroupItem resolveExpressionItem;
    TextInput textControl;
    HTMLElement expressionMode;
    HTMLElement normalMode;

    FormItem(String identifier, ResourceAttribute ra, FormGroupLabel label) {
        this.identifier = identifier;
        this.ra = ra;
        this.label = label;
        this.switchToExpressionModeId = Id.build(identifier, "switch-to-expression-mode");
        this.switchToNormalModeId = Id.build(identifier, "switch-to-normal-mode");
        this.resolveExpressionId = Id.build(identifier, "resolve-expression");
        this.data = new HashMap<>();
    }

    @Override
    public HTMLElement element() {
        if (formGroup == null) {
            logger.error("Element for form item %s has not been initialized!", identifier);
            return span().element();
        }
        return formGroup.element();
    }

    @Override
    public FormItem that() {
        return this;
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public <T> FormItem store(String key, T value) {
        data.put(key, value);
        return this;
    }

    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (data.containsKey(key)) {
            return (T) data.get(key);
        }
        return null;
    }

    // ------------------------------------------------------ building blocks

    void defaultSetup() {
        if (ra.description.readOnly()) {
            formGroupControl = readOnlyGroup();
        } else {
            if (ra.description.expressionAllowed()) {
                expressionMode = expressionMode();
                normalMode = normalMode();
                formGroupControl = formGroupControl();
                if (ra.expression) {
                    switchToExpressionMode();
                    toggleResolveExpression(ra.value.asString());
                } else {
                    switchToNormalMode();
                }
            } else {
                formGroupControl = nativeGroup();
            }
        }
        formGroup = formGroup(identifier)
                .required(ra.description.required())
                .addLabel(label)
                .addControl(formGroupControl);
    }

    /** must be overridden in subclasses if {@link #defaultSetup()} is used */
    FormGroupControl readOnlyGroup() {
        return formGroupControl();
    }

    /** must be overridden in subclasses if {@link #defaultSetup()} is used */
    FormGroupControl nativeGroup() {
        return formGroupControl();
    }

    /** must be overridden in subclasses if {@link #defaultSetup()} is used */
    HTMLElement normalMode() {
        return div().element();
    }

    /** may be overridden in subclasses */
    HTMLElement expressionMode() {
        return inputGroup()
                .addItem(inputGroupItem()
                        .addButton(switchToNormalModeButton()))
                .addItem(inputGroupItem().fill()
                        .addControl(textControl()
                                .onChange((event, component, value) -> toggleResolveExpression(value))))
                .addItem(resolveExpressionItem = inputGroupItem()
                        .addButton(resolveExpressionButton()))
                .run(ig -> {
                    if (ra.description.unit() != null) {
                        ig.addText(unitInputGroupText());
                    }
                })
                .element();
    }

    TextInput textControl() {
        if (textControl == null) {
            textControl = textInput(identifier)
                    .run(ti -> {
                        if (ra.value.isDefined()) {
                            ti.value(ra.value.asString());
                        } else {
                            ti.placeholder("undefined");
                        }
                    });
        }
        return textControl;
    }

    InputGroupText unitInputGroupText() {
        return inputGroupText().plain().add(small().textContent(ra.description.unit()));
    }

    Button resolveExpressionButton() {
        return button().id(resolveExpressionId).control().icon(link())
                .onClick((e, b) -> {
                    if (textControl != null) {
                        logger.info("Resolve expression: %s", textControl.value());
                        // TODO Resolve expression
                    }
                });
    }

    Button switchToExpressionModeButton() {
        return button().id(switchToExpressionModeId).control().icon(dollarSign())
                .onClick((e, b) -> switchToExpressionMode());
    }

    Button switchToNormalModeButton() {
        return button().id(switchToNormalModeId).control().icon(terminal())
                .onClick((e, b) -> switchToNormalMode());
    }

    // ------------------------------------------------------ event handlers

    void switchToExpressionMode() {
        failSafeRemoveFromParent(normalMode);
        formGroupControl.add(expressionMode);
        tooltip(By.id(switchToNormalModeId), "Switch to normal mode").appendTo(expressionMode);
        tooltip(By.id(resolveExpressionId), "Resolve expression").appendTo(expressionMode);

    }

    void switchToNormalMode() {
        failSafeRemoveFromParent(expressionMode);
        formGroupControl.add(normalMode);
        tooltip(By.id(switchToExpressionModeId), "Switch to expression mode").appendTo(normalMode);
    }

    void toggleResolveExpression(String value) {
        if (resolveExpressionItem != null) {
            setVisible(resolveExpressionItem, isExpression(value));
        }
    }
}
