package org.jboss.hal.meta;

@FunctionalInterface
interface RemovalHandler<K, V> {

    void onRemoval(K key, V value);
}
