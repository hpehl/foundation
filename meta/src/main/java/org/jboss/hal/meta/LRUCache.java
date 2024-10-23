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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class LRUCache<K, V> {

    private final int capacity;
    private final LinkedList<Node<K, V>> cacheList;
    private final HashMap<K, Node<K, V>> cacheMap;
    private final List<RemovalHandler<K, V>> removalHandlers;

    LRUCache(int capacity) {
        this.capacity = capacity;
        this.cacheList = new LinkedList<>();
        this.cacheMap = new HashMap<>();
        this.removalHandlers = new ArrayList<>();
    }

    // ------------------------------------------------------ api

    boolean contains(K key) {
        return cacheMap.containsKey(key);
    }

    int size() {
        return cacheMap.size();
    }

    V get(K key) {
        Node<K, V> node = cacheMap.get(key);
        if (node == null) {
            return null;
        }

        moveToHead(node);
        return node.value;
    }

    void put(K key, V value) {
        Node<K, V> existingNode = cacheMap.get(key);
        if (existingNode != null) {
            existingNode.value = value;
            moveToHead(existingNode);
            return;
        }

        Node<K, V> newNode = new Node<>(key, value);
        cacheList.addFirst(newNode);
        cacheMap.put(key, newNode);

        if (cacheList.size() > capacity) {
            removeLeastRecentlyUsed();
        }
    }

    V remove(K key) {
        Node<K, V> existingNode = cacheMap.remove(key);
        if (existingNode != null) {
            cacheList.remove(existingNode);
            return existingNode.value;
        }
        return null;
    }

    void addRemovalHandler(RemovalHandler<K, V> handler) {
        removalHandlers.add(handler);
    }

    // ------------------------------------------------------ internal

    // for testing purposes
    @SuppressWarnings("unchecked")
    K[] keys() {
        return (K[]) cacheList.stream().map(n -> n.key).toArray();
    }

    Set<Map.Entry<K, Node<K, V>>> entries() {
        return cacheMap.entrySet();
    }

    private void moveToHead(Node<K, V> node) {
        cacheList.remove(node);
        cacheList.addFirst(node);
    }

    private void removeLeastRecentlyUsed() {
        Node<K, V> tail = cacheList.removeLast();
        cacheMap.remove(tail.key);
        for (RemovalHandler<K, V> removalHandler : removalHandlers) {
            removalHandler.onRemoval(tail.key, tail.value);
        }
    }

    // ------------------------------------------------------ inner classes

    static class Node<K, V> {

        final K key;
        V value;

        private Node(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {return true;}
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Node<?, ?> node = (Node<?, ?>) o;
            return Objects.equals(key, node.key);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key);
        }
    }
}

