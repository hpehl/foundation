package org.jboss.hal.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

class Cache<K, V> {

    private final int capacity;
    private final BiConsumer<K, V> removalHandler;
    private final DoublyLinkedList<K, V> cacheList;
    private final HashMap<K, Node<K, V>> cacheMap;

    Cache(int capacity) {
        this(capacity, null);
    }

    Cache(int capacity, BiConsumer<K, V> removalHandler) {
        this.capacity = capacity;
        this.removalHandler = removalHandler;
        this.cacheList = new DoublyLinkedList<>();
        this.cacheMap = new HashMap<>();
    }

    public V get(K key) {
        Node<K, V> node = cacheMap.get(key);
        if (node == null) {
            return null;
        }

        moveToHead(node);
        return node.value;
    }

    public void put(K key, V value) {
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

    public V remove(K key) {
        Node<K, V> existingNode = cacheMap.remove(key);
        if (existingNode != null) {
            cacheList.remove(existingNode);
            return existingNode.value;
        }
        return null;
    }

    // for testing purposes
    K[] lruKeys() {
        List<K> keys = new ArrayList<>();
        Node<K, V> current = cacheList.head;
        while (current != null) {
            keys.add(current.key);
            current = current.next;
        }
        //noinspection unchecked
        return (K[]) keys.toArray();
    }

    private void moveToHead(Node<K, V> node) {
        cacheList.remove(node);
        cacheList.addFirst(node);
    }

    private void removeLeastRecentlyUsed() {
        Node<K, V> tail = cacheList.removeLast();
        cacheMap.remove(tail.key);
        if (removalHandler != null) {
            removalHandler.accept(tail.key, tail.value);
        }
    }

    // ------------------------------------------------------ inner classes

    private static class Node<K, V> {

        private final K key;
        private V value;
        private Node<K, V> prev;
        private Node<K, V> next;

        private Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private static class DoublyLinkedList<K, V> {

        private Node<K, V> head;
        private Node<K, V> tail;

        private void addFirst(Node<K, V> node) {
            if (isEmpty()) {
                head = tail = node;
            } else {
                node.next = head;
                head.prev = node;
                head = node;
            }
        }

        private void remove(Node<K, V> node) {
            if (node == head) {
                head = head.next;
            } else if (node == tail) {
                tail = tail.prev;
            }

            if (node.prev != null) {
                node.prev.next = node.next;
            }
            if (node.next != null) {
                node.next.prev = node.prev;
            }
        }

        private Node<K, V> removeLast() {
            if (isEmpty()) {
                throw new IllegalStateException("List is empty");
            }

            Node<K, V> last = tail;
            remove(last);
            return last;
        }

        private boolean isEmpty() {
            return head == null;
        }

        private int size() {
            int size = 0;
            Node<K, V> current = head;
            while (current != null) {
                size++;
                current = current.next;
            }
            return size;
        }
    }
}

