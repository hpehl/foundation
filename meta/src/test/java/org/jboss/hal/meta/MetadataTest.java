package org.jboss.hal.meta;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class MetadataTest {

    @Test
    void undefined() {
        Metadata metadata = Metadata.undefined();
        assertFalse(metadata.isDefined());
        assertFalse(metadata.resourceDescription().isDefined());
        assertFalse(metadata.securityContext().isDefined());
    }
}
