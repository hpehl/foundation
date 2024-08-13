package org.jboss.hal.meta.description;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AttributeDescriptionTest {

    @Test
    void undefined() {
        AttributeDescription attributeDescription = new AttributeDescription();
        assertFalse(attributeDescription.isDefined());
        assertEquals("undefined", attributeDescription.description());
        assertFalse(attributeDescription.deprecation().isDefined());
        assertEquals("", attributeDescription.formatType());
        assertFalse(attributeDescription.simpleRecord());
    }
}