package org.jboss.hal.meta;

import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("GrazieInspection")
class CacheTest {

    @Test
    void lifecycle() {
        String value;
        Recorder recorder = new Recorder();
        Cache<Integer, String> cache = new Cache<>(3, recorder);

        // populate (3:C, 2:B, 1:A)
        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");
        assertNull(recorder.lastKey);
        assertNull(recorder.lastValue);
        assertArrayEquals(new Integer[]{3, 2, 1}, cache.lruKeys());

        // new entry (4:D, 3:C, 2:B) [1:A]
        cache.put(4, "D");
        assertEquals(1, recorder.lastKey);
        assertEquals("A", recorder.lastValue);
        assertArrayEquals(new Integer[]{4, 3, 2}, cache.lruKeys());

        // update entry (2:b, 4:D, 3:C)
        cache.put(2, "b");
        assertEquals(1, recorder.lastKey);
        assertEquals("A", recorder.lastValue);
        assertArrayEquals(new Integer[]{2, 4, 3}, cache.lruKeys());

        // get entry (3:C, 2:b, 4:D)
        value = cache.get(3);
        assertEquals("C", value);
        assertEquals(1, recorder.lastKey);
        assertEquals("A", recorder.lastValue);
        assertArrayEquals(new Integer[]{3, 2, 4}, cache.lruKeys());

        // new entry (5:E, 3:C, 2:b) [4:D]
        cache.put(5, "E");
        assertEquals(4, recorder.lastKey);
        assertEquals("D", recorder.lastValue);
        assertArrayEquals(new Integer[]{5, 3, 2}, cache.lruKeys());

        // remove entry (5:E, 2:b)
        value = cache.remove(3);
        assertEquals("C", value);
        assertEquals(4, recorder.lastKey);
        assertEquals("D", recorder.lastValue);
        assertArrayEquals(new Integer[]{5, 2}, cache.lruKeys());

        // new entry (6:F, 5:E, 2:b)
        cache.put(6, "F");
        assertEquals(4, recorder.lastKey);
        assertEquals("D", recorder.lastValue);
        assertArrayEquals(new Integer[]{6, 5, 2}, cache.lruKeys());

        // new entry (7:G, 6:F, 5:E) [2:b]
        cache.put(7, "G");
        assertEquals(2, recorder.lastKey);
        assertEquals("b", recorder.lastValue);
        assertArrayEquals(new Integer[]{7, 6, 5}, cache.lruKeys());
    }

    private static class Recorder implements BiConsumer<Integer, String> {

        Integer lastKey = null;
        String lastValue = null;

        @Override
        public void accept(Integer key, String value) {
            this.lastKey = key;
            this.lastValue = value;
        }
    }
}