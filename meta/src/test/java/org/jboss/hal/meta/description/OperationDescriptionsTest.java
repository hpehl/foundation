package org.jboss.hal.meta.description;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationDescriptionsTest {

    @Test
    void undefined() {
        OperationDescriptions operationDescriptions = new OperationDescriptions();
        assertTrue(operationDescriptions.isEmpty());
        assertFalse(operationDescriptions.get("foo").isDefined());
    }
}