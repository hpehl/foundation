package org.jboss.hal.resources;

/**
 * Keys used to store data in map-like structures like {@code org.patternfly.core.ComponentContext} and
 * {@code org.jboss.elemento.flow.FlowContext}.
 */
public interface Keys {

    // ------------------------------------------------------ IDs (a-z)

    /** org.jboss.hal.ui.filter.AccessTypeValue */
    String ACCESS_TYPE_VALUE = "access-type-value";

    /** org.jboss.hal.meta.description.AttributeDescription */
    String ATTRIBUTE_DESCRIPTION = "attribute-description";

    /** List<String> */
    String HOSTS = "hosts";

    /** org.jboss.hal.ui.modelbrowser.ModelBrowserNode */
    String MODEL_BROWSER_NODE = "model-browser-node";

    /** org.jboss.hal.meta.description.OperationDescription */
    String OPERATION_DESCRIPTION = "operation-description";

    /** org.jboss.hal.ui.resource.ResourceAttribute */
    String RESOURCE_ATTRIBUTE = "resource-attribute";

    /** org.jboss.hal.ui.filter.StorageValue */
    String STORAGE_VALUE = "storage-value";

    /** List<String> */
    String PROVIDER_POINTS = "provider-points";

    /** List<AddressTemplate> */
    String ADDRESS_TEMPLATES = "address-templates";

    /** org.jboss.hal.ui.filter.TypeValues */
    String TYPE_VALUES = "type-values";
}
