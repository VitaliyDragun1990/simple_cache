package com.revenat.simple_cache.cache;

import java.util.Collection;

public interface CacheEngine<K, V> {

    void put(CacheElement<K, V> element);

    CacheElement<K, V> get(K key);

    void delete(K key);

    void deleteAll(Collection<K> elementKeys);

    void deleteAll();

    int getHitCount();

    int getMissCount();

    void dispose();
}
