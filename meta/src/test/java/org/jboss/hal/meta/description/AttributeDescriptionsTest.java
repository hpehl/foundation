package org.jboss.hal.meta.description;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributeDescriptionsTest {

    @Test
    void undefined() {
        AttributeDescriptions attributeDescriptions = new AttributeDescriptions();
        assertTrue(attributeDescriptions.isEmpty());
        assertFalse(attributeDescriptions.get("foo").isDefined());
    }
}