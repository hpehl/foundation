package org.jboss.hal.meta.description;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationDescriptionTest {

    @Test
    void undefined() {
        OperationDescription operationDescription = new OperationDescription();
        assertFalse(operationDescription.isDefined());
        assertEquals("undefined", operationDescription.description());
        assertFalse(operationDescription.deprecation().isDefined());
        assertFalse(operationDescription.global());
        assertTrue(operationDescription.parameters().isEmpty());
        assertFalse(operationDescription.returnValue().isDefined());
    }
}
