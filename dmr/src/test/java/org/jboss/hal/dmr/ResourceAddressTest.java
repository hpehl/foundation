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
package org.jboss.hal.dmr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceAddressTest {

    @Test
    void nil() {
        assertThrows(IllegalArgumentException.class, () -> ResourceAddress.from(null));
    }

    @Test
    void empty() {
        assertEquals("/", ResourceAddress.from("").toString());
        assertEquals("/", ResourceAddress.from("  ").toString());
        assertEquals("/", ResourceAddress.from("/").toString());
        assertEquals("/", ResourceAddress.from("/  ").toString());
        assertEquals("/", ResourceAddress.from("  /").toString());
        assertEquals("/", ResourceAddress.from("  /  ").toString());
    }

    @Test
    void rootIsEmpty() {
        assertTrue(ResourceAddress.root().isEmpty());
        assertEquals(0, ResourceAddress.root().size());
        assertEquals("/", ResourceAddress.root().toString());
    }

    @Test
    void fromString() {
        assertEquals("/", ResourceAddress.from("").toString());
        assertEquals("/", ResourceAddress.from("/").toString());
        assertEquals("/a=1", ResourceAddress.from("a=1").toString());
        assertEquals("/a=1", ResourceAddress.from("a=1/").toString());
        assertEquals("/a=1", ResourceAddress.from("/a=1").toString());
        assertEquals("/a=1", ResourceAddress.from("/a=1/").toString());
        assertEquals("/a=1/b=2/c=3", ResourceAddress.from("a=1/b=2/c=3").toString());
    }

    @Test
    void malformed() {
        assertThrows(IllegalArgumentException.class, () -> ResourceAddress.from("//"));
        assertThrows(IllegalArgumentException.class, () -> ResourceAddress.from("///"));
        assertThrows(IllegalArgumentException.class, () -> ResourceAddress.from("////"));
        assertThrows(IllegalArgumentException.class, () -> ResourceAddress.from("a"));
        assertThrows(IllegalArgumentException.class, () -> ResourceAddress.from("/a"));
        assertThrows(IllegalArgumentException.class, () -> ResourceAddress.from("a/"));
        assertThrows(IllegalArgumentException.class, () -> ResourceAddress.from("a/b"));
        assertThrows(IllegalArgumentException.class, () -> ResourceAddress.from("a/b=1"));
        assertThrows(IllegalArgumentException.class, () -> ResourceAddress.from("a=1/b"));
    }

    @Test
    void add() {
        ResourceAddress empty = ResourceAddress.from("");
        ResourceAddress a = ResourceAddress.from("a=1");
        ResourceAddress b = ResourceAddress.from("b=2");
        assertEquals("/a=1", empty.add("a", "1").toString());
        assertEquals("/a=1/b=2", a.add(b).toString());
    }

    @Test
    void size() {
        ResourceAddress empty = ResourceAddress.from("");
        ResourceAddress a = ResourceAddress.from("a=1");
        ResourceAddress ab = ResourceAddress.from("a=1/b=2");

        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
        assertFalse(a.isEmpty());
        assertEquals(1, a.size());
        assertFalse(ab.isEmpty());
        assertEquals(2, ab.size());
    }
}