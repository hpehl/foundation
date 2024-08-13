package org.jboss.hal.ui.resource;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.AttributeDescription;

/** Simple record for an attribute name/value/description triple in {@link ResourceView} and {@link ResourceForm}. */
class ResourceAttribute {

    final String name;
    final ModelNode value;
    final AttributeDescription description;

    ResourceAttribute(String name, ModelNode value, AttributeDescription description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }
}
