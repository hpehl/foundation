package org.jboss.hal.meta.description;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceDescriptionTest {

    @Test
    void undefined() {
        ResourceDescription resourceDescription = new ResourceDescription();
        assertFalse(resourceDescription.isDefined());
        assertTrue(resourceDescription.attributes().isEmpty());
        assertTrue(resourceDescription.operations().isEmpty());
        assertEquals("undefined", resourceDescription.description());
        assertFalse(resourceDescription.deprecation().isDefined());
    }
}