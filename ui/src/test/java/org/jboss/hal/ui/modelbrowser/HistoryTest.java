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
package org.jboss.hal.ui.modelbrowser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryTest {

    @Test
    void navigate() {
        History<String> history = new History<>();

        // At the start, there should be nothing to go back or forward to
        assertNull(history.current());
        assertFalse(history.canGoBack());
        assertFalse(history.canGoForward());

        // Navigate to the first item
        history.navigate("1");
        assertEquals("1", history.current());
        assertFalse(history.canGoBack());
        assertFalse(history.canGoForward());

        // Navigate to the second item
        history.navigate("2");
        assertEquals("2", history.current());
        assertTrue(history.canGoBack());
        assertFalse(history.canGoForward());

        // Navigate back
        assertEquals("1", history.back());
        assertEquals("1", history.current());
        assertFalse(history.canGoBack());
        assertTrue(history.canGoForward());

        // Navigate to the third item
        history.navigate("3");
        assertEquals("3", history.current());
        assertTrue(history.canGoBack());
        assertFalse(history.canGoForward());

        // Navigate back again
        assertEquals("1", history.back());
        assertEquals("1", history.current());
        assertFalse(history.canGoBack());
        assertTrue(history.canGoForward());

        // Navigate forward
        assertEquals("3", history.forward());
        assertEquals("3", history.current());
        assertTrue(history.canGoBack());
        assertFalse(history.canGoForward());

        // Navigate forward again
        assertEquals("3", history.forward());
        assertEquals("3", history.current());
        assertTrue(history.canGoBack());
        assertFalse(history.canGoForward());
    }

    @Test
    void backAndForth() {
        History<Integer> history = new History<>();
        for (int i = 0; i <= 10; i++) {
            history.navigate(i);
        }
        for (int i = 9; i >= 0; i--) {
            assertEquals(i, (int) history.back());
        }
        for (int i = 1; i < 10; i++) {
            assertEquals(i, (int) history.forward());
        }
    }
}
