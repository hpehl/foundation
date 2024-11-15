package org.jboss.hal.ui.resource;

import java.util.List;

import org.jboss.hal.core.LabelBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.WildcardResolver;
import org.jboss.hal.meta.description.OperationDescription;
import org.jboss.hal.ui.resource.FormItemFlags.Placeholder;
import org.patternfly.component.modal.Modal;

import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.meta.WildcardResolver.Direction.LTR;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.FormItemFactory.formItem;
import static org.jboss.hal.ui.resource.FormItemFactory.nameFormItem;
import static org.jboss.hal.ui.resource.ResourceAttribute.notDeprecated;
import static org.jboss.hal.ui.resource.ResourceAttribute.resourceAttributes;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.alert.Alert.alert;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.modal.Modal.modal;
import static org.patternfly.component.modal.ModalBody.modalBody;
import static org.patternfly.component.modal.ModalFooter.modalFooter;
import static org.patternfly.component.modal.ModalHeader.modalHeader;
import static org.patternfly.style.Size.lg;

public class AddResourceDialog {

    // ------------------------------------------------------ factory

    public static AddResourceDialog addResourceDialog(AddressTemplate parent, String resource, boolean singleton) {
        return new AddResourceDialog(parent, resource, singleton);
    }

    // ------------------------------------------------------ instance

    private final boolean singleton;
    private final AddressTemplate template;
    private final String title;

    AddResourceDialog(AddressTemplate parent, String resource, boolean singleton) {
        this.singleton = singleton;
        if (singleton) {
            title = "Add " + resource;
            template = new WildcardResolver(LTR, resource).resolve(parent);
        } else {
            title = new LabelBuilder().label(parent.last().key);
            template = AddressTemplate.of(parent);
        }
    }

    // ------------------------------------------------------ api

    public Promise<ModelNode> add() {
        return new Promise<>((resolve, reject) -> uic().metadataRepository().lookup(template)
                .then(Promise::resolve)
                .then(metadata -> {
                    OperationDescription operation = metadata.resourceDescription().operations().get(ADD);
                    if (operation.isDefined()) {
                        List<ResourceAttribute> resourceAttributes = resourceAttributes(metadata, operation.parameters(),
                                notDeprecated());
                        ResourceForm resourceForm = new ResourceForm(template);
                        if (!singleton) {
                            resourceForm.addItem(nameFormItem(metadata));
                        }
                        for (ResourceAttribute ra : resourceAttributes) {
                            resourceForm.addItem(
                                    formItem(template, metadata, ra, new FormItemFlags(Placeholder.DEFAULT_VALUE)));
                        }
                        modal().size(lg).top()
                                .addHeader(modalHeader()
                                        .addTitle(title)
                                        .addDescription(metadata.resourceDescription().description()))
                                .addBody(modalBody()
                                        .add(div().css(halComponent(resource))
                                                .add(resourceForm)))
                                .addFooter(modalFooter()
                                        .addButton(button("Add").primary(),
                                                (__, modal) -> internalAdd(modal, resourceForm, resolve))
                                        .addButton(button("Cancel").link(), (__, modal) -> {
                                            modal.close();
                                            resolve.onInvoke(new ModelNode());
                                        }))
                                .appendToBody()
                                .open();
                    } else {
                        reject.onInvoke("No add operation defined for " + template);
                    }
                    return null;
                })
                .catch_(error -> {
                    reject.onInvoke(error);
                    return null;
                }));
    }

    // ------------------------------------------------------ internal

    private void internalAdd(Modal modal, ResourceForm resourceForm, ResolveCallbackFn<ModelNode> resolve) {
        resourceForm.resetValidation();
        if (resourceForm.validate()) {
            AddressTemplate fqTemplate;
            ModelNode payload = resourceForm.modelNode();
            if (payload.has(NAME)) {
                ModelNode nameModelNode = payload.remove(NAME);
                fqTemplate = new WildcardResolver(LTR, nameModelNode.asString()).resolve(template);
            } else {
                fqTemplate = AddressTemplate.of(template);
            }
            uic().crud().create(fqTemplate, payload)
                    .then(__ -> {
                        modal.close();
                        resolve.onInvoke(payload);
                        return null;
                    })
                    .catch_(error -> {
                        resourceForm.addAlert(alert(danger, "Failed to add resource").inline()
                                .addDescription(String.valueOf(error)));
                        return null;
                    });
        } else {
            resourceForm.addAlert(alert(danger, "Failed to add resource").inline()
                    .addDescription("There are validation errors. Please fix them and try again."));
        }
    }
}
