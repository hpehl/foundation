package org.jboss.hal.ui.resource;

import org.jboss.elemento.Id;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Keys;
import org.jboss.hal.ui.LabelBuilder;
import org.jboss.hal.ui.UIContext;
import org.patternfly.component.form.FormGroup;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;

import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRED;
import static org.jboss.hal.ui.BuildingBlocks.attributeDescriptionPopover;
import static org.patternfly.component.form.FormGroup.formGroup;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormGroupLabel.formGroupLabel;
import static org.patternfly.component.form.TextInput.textInput;

class FormFactory {


    // ------------------------------------------------------ form

    static FormGroup formItem(UIContext uic, AddressTemplate template, Metadata metadata, ResourceAttribute ra) {
        String identifier = Id.build(ra.fqn, "edit");
        return formGroup(identifier)
                .store(Keys.RESOURCE_ATTRIBUTE, ra)
                .fieldId(identifier)
                .required(ra.description.get(REQUIRED).asBoolean(false))
                .addLabel(label(ra))
                .addControl(control(identifier));
    }

    private static FormGroupLabel label(ResourceAttribute ra) {
        LabelBuilder labelBuilder = new LabelBuilder();
        String label = labelBuilder.label(ra.name);
        return formGroupLabel(label)
                .help(label + " description", attributeDescriptionPopover(label, ra.description));
    }

    private static FormGroupControl control(String id) {
        return formGroupControl()
                .addControl(textInput(id));
    }
}
