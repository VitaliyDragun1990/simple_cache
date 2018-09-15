package com.revenat.simple_cache.cacheImpl;

import com.revenat.simple_cache.cache.CacheElement;
import com.revenat.simple_cache.cache.CacheEngine;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.function.Function;

public class CacheEngineImpl<K, V> implements CacheEngine<K, V> {
    private static final int TIME_THRESHOLD_MS = 5;

    private final int maxElements;
    private final long lifeTimeMs;
    private final long idleTimeMs;
    private final boolean isEternal;

    private final Map<K, SoftReference<CacheElement<K, V>>> cacheElements = new LinkedHashMap<>();
    private final Timer timer = new Timer();

    private int hitCounter = 0;
    private int missCounter = 0;

    public CacheEngineImpl(CacheConfiguration conf) {
        this.maxElements = conf.getMaxElements();
        this.lifeTimeMs = conf.getLifeTimeMs() > 0 ? conf.getLifeTimeMs() : 0;
        this.idleTimeMs = conf.getIdleTimeMs() > 0 ? conf.getIdleTimeMs() : 0;
        this.isEternal = (lifeTimeMs == 0 && idleTimeMs == 0) || conf.isEternal();
    }

    @Override
    public void put(CacheElement<K, V> element) {
        allocateSpaceIfFull();

        addElementIntoCache(element);

        createTimerTasksForAddedElement(element);
    }

    private void allocateSpaceIfFull() {
        if (isFull()) {
            K firstKey = cacheElements.keySet().iterator().next();
            cacheElements.remove(firstKey);
        }
    }

    private boolean isFull() {
        return cacheElements.size() == maxElements;
    }

    private void addElementIntoCache(CacheElement<K, V> element) {
        K key = element.getKey();
        cacheElements.put(key, new SoftReference<>(element));
    }

    private void createTimerTasksForAddedElement(CacheElement<K, V> element) {
        K elementKey = element.getKey();
        if (!isEternal) {
            if (lifeTimeMs != 0) {
                TimerTask lifeTimerTask = getTimerTask(elementKey, lifeElement -> lifeElement.getCreationTime() + lifeTimeMs);
                timer.schedule(lifeTimerTask, lifeTimeMs);
            }
            if (idleTimeMs != 0) {
                TimerTask idleTimerTask = getTimerTask(elementKey, idleElement -> idleElement.getLastAccessTime() + idleTimeMs);
                timer.schedule(idleTimerTask, idleTimeMs, idleTimeMs);
            }
        }
    }

    @Override
    public CacheElement<K, V> get(K key) {
        CacheElement<K, V> element = getElementByKey(key);

        incrementResultCounter(element);

        return element;
    }

    private CacheElement<K, V> getElementByKey(K key) {
        SoftReference<CacheElement<K, V>> elementRef = cacheElements.get(key);
        return elementRef != null ? elementRef.get() : null;
    }

    private void incrementResultCounter(CacheElement<K, V> element) {
        if (element != null) {
            hitCounter++;
            element.setAccessed();
        } else {
            missCounter++;
        }
    }

    @Override
    public void delete(K key) {
        cacheElements.remove(key);
    }

    @Override
    public void deleteAll(Collection<K> elementKeys) {
        for (K key : elementKeys) {
            cacheElements.remove(key);
        }
    }

    @Override
    public void deleteAll() {
        cacheElements.clear();
    }

    @Override
    public int getHitCount() {
        return this.hitCounter;
    }

    @Override
    public int getMissCount() {
        return this.missCounter;
    }

    @Override
    public void dispose() {
        this.timer.cancel();
    }

    private TimerTask getTimerTask(final K key, Function<CacheElement<K, V>, Long> timeFunction) {
        return new TimerTask() {
            @Override
            public void run() {
                CacheElement<K, V> element = getElementByKey(key);

                if (element == null ||
                        isTime1BeforeTime2(timeFunction.apply(element), System.currentTimeMillis())) {
                    cacheElements.remove(key);
                    this.cancel();
                }
            }
        };
    }

    private boolean isTime1BeforeTime2(long time1, long time2) {
        return time1 < time2 + TIME_THRESHOLD_MS;
    }
}
