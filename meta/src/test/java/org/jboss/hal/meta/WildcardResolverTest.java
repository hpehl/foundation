/*
 *  Copyright 2024 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.meta;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WildcardResolverTest {

    @Test
    void resolve() {
        WildcardResolver resolver = new WildcardResolver(null);
        assertEquals("a=b", resolver.resolve(AddressTemplate.of("a=b")).template);

        resolver = new WildcardResolver(null, (String[]) null);
        assertEquals("a=b", resolver.resolve(AddressTemplate.of("a=b")).template);

        resolver = new WildcardResolver("foo");
        assertEquals("a=b", resolver.resolve(AddressTemplate.of("a=b")).template);

        resolver = new WildcardResolver("foo");
        assertEquals("{a}/b={c}", resolver.resolve(AddressTemplate.of("{a}/b={c}")).template);

        resolver = new WildcardResolver("b");
        assertEquals("a=b/c=*", resolver.resolve(AddressTemplate.of("a=*/c=*")).template);

        resolver = new WildcardResolver("b", "d");
        assertEquals("a=b/c=d", resolver.resolve(AddressTemplate.of("a=*/c=*")).template);

        resolver = new WildcardResolver("b", "d", "e");
        assertEquals("a=b/c=d", resolver.resolve(AddressTemplate.of("a=*/c=*")).template);

        resolver = new WildcardResolver("b", "d", "e");
        assertEquals("a=b/c={d}", resolver.resolve(AddressTemplate.of("a=*/c={d}")).template);
    }
}
