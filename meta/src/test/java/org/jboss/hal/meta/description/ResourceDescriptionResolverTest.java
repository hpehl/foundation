package org.jboss.hal.meta.description;

import org.jboss.hal.meta.AddressTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDescriptionResolverTest {

    @Test
    void resolve() {
        ResourceDescriptionResolver resolver = new ResourceDescriptionResolver();
        assertEquals("a=b/c=d/e=*", resolver.resolve(AddressTemplate.of("a=b/c=d/e=f")).template);
    }
}
