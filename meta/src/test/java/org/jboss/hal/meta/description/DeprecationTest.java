package org.jboss.hal.meta.description;

import org.jboss.hal.env.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DeprecationTest {

    @Test
    void undefined() {
        Deprecation deprecation = new Deprecation();
        assertFalse(deprecation.isDefined());
        assertEquals(Version.EMPTY_VERSION, deprecation.since());
        assertEquals("undefined", deprecation.reason());
    }
}