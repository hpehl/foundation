package org.jboss.hal.meta.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class SecurityContextTest {

    @Test
    void undefined() {
        SecurityContext securityContext = new SecurityContext();
        assertFalse(securityContext.isDefined());
        assertFalse(securityContext.readable());
        assertFalse(securityContext.writable());
        assertFalse(securityContext.readable("foo"));
        assertFalse(securityContext.writable("foo"));
        assertFalse(securityContext.executable("foo"));
    }
}
