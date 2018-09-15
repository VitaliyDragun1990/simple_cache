package com.revenat.simple_cache.cacheImpl;

import com.revenat.simple_cache.cache.CacheElement;
import com.revenat.simple_cache.cache.CacheEngine;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class CacheEngineImplTest {
    private final static int KEY_A = 1;
    private final static int KEY_B = 2;
    private final static int KEY_C = 3;
    private final static String VALUE_A = "A";
    private final static String VALUE_B = "B";
    private final static String VALUE_C = "C";

    private final static CacheElement<Integer, String> ELEMENT_A = new CacheElement<>(KEY_A, VALUE_A);
    private final static CacheElement<Integer, String> ELEMENT_B = new CacheElement<>(KEY_B, VALUE_B);
    private final static CacheElement<Integer, String> ELEMENT_C = new CacheElement<>(KEY_C, VALUE_C);

    private CacheEngine<Integer, String> cache;

    @Before
    public void setUp() throws Exception {
        CacheConfiguration configuration = CacheConfiguration.forEternalCache(2);
        cache = new CacheEngineImpl<>(configuration);
    }

    @Test
    public void returnsNullIfNoSuchElementInCache() {
        assertNull(cache.get(KEY_A));
    }

    @Test
    public void putsElementIntoCache() {
        cache.put(ELEMENT_A);

        CacheElement<Integer, String> retrievedElement = cache.get(KEY_A);
        assertSame(ELEMENT_A, retrievedElement);
    }

    @Test
    public void replacesPreviousValueIfPutsWithSameKey() {
        cache.put(new CacheElement<>(KEY_A, VALUE_A));
        cache.put(new CacheElement<>(KEY_A, VALUE_B));

        CacheElement<Integer, String> element = cache.get(KEY_A);

        assertThat(element.getValue(), equalTo(VALUE_B));
    }

    @Test
    public void removesFirstElementsIfPutAboveMaxSize() {
        cache.put(ELEMENT_A);
        cache.put(ELEMENT_B);
        cache.put(ELEMENT_C);

        assertNull(cache.get(KEY_A));
        assertSame(ELEMENT_B, cache.get(KEY_B));
        assertSame(ELEMENT_C, cache.get(KEY_C));
    }

    @Test
    public void incrementsOnlyHitCountIfCanGetElement() {
        int initialHitCount = cache.getHitCount();
        int initialMissCount = cache.getMissCount();
        cache.put(ELEMENT_A);

        cache.get(KEY_A);

        int incrementedHitCount = cache.getHitCount();
        assertThat(incrementedHitCount, equalTo(initialHitCount + 1));
        assertThat(cache.getMissCount(), equalTo(initialMissCount));
    }

    @Test
    public void incrementsOnlyMissCountIfCanNotGetElement() {
        int initialMissCount = cache.getMissCount();
        int initialHitCount = cache.getHitCount();

        cache.get(KEY_A);

        int incrementedMissCount = cache.getMissCount();
        assertThat(incrementedMissCount, equalTo(initialMissCount + 1));
        assertThat(cache.getHitCount(), equalTo(initialHitCount));
    }

    @Test
    public void deletesElementFromCacheIfExists() {
        cache.put(ELEMENT_A);

        cache.delete(KEY_A);

        assertNull(cache.get(KEY_A));
    }

    @Test
    public void deletesAllElementsWithGivenKeys() {
        cache.put(ELEMENT_A);
        cache.put(ELEMENT_B);

        cache.deleteAll(Arrays.asList(KEY_A, KEY_C));

        assertNull(cache.get(KEY_A));
        assertNotNull(cache.get(KEY_B));
    }

    @Test
    public void deletesAllElementsFromCache() {
        cache.put(ELEMENT_A);
        cache.put(ELEMENT_B);

        cache.deleteAll();

        assertNull(cache.get(KEY_A));
        assertNull(cache.get(KEY_B));
    }

    @Test
    public void removesElementsIfAboveLifeTimeInLiveCache() throws InterruptedException {
        cache = new CacheEngineImpl<>(CacheConfiguration.forLiveCache(2, 300));
        cache.put(ELEMENT_A);
        cache.put(ELEMENT_B);

        Thread.sleep(500);

        assertNull(cache.get(KEY_A));
        assertNull(cache.get(KEY_B));
    }

    @Test
    public void removesElementsIfNoAccessedInIdleCache() throws InterruptedException {
        cache = new CacheEngineImpl<>(CacheConfiguration.forIdleCache(2, 500));
        cache.put(ELEMENT_A);
        cache.put(ELEMENT_B);

        Thread.sleep(200);
        cache.get(KEY_A);
        Thread.sleep(350);

        assertNotNull(cache.get(KEY_A));
        assertNull(cache.get(KEY_B));
    }

    @Test
    public void removesElementIfAboveLifeTimeOrNotAccessedInLiveIdleCache() throws InterruptedException {
        cache = new CacheEngineImpl<>(CacheConfiguration.forLiveAndIdleCache(2, 1000, 500));
        cache.put(ELEMENT_A);
        cache.put(ELEMENT_B);

        Thread.sleep(300);
        cache.get(KEY_A);
        Thread.sleep(300);

        assertNull(cache.get(KEY_B));
        assertNotNull(cache.get(KEY_A));

        Thread.sleep(500);

        assertNull(cache.get(KEY_A));
    }

    @Test(expected = IllegalStateException.class)
    public void throwsExceptionIfPutAfterDisposeInLiveCache() {
        cache = new CacheEngineImpl<>(CacheConfiguration.forLiveCache(2, 100));

        cache.dispose();

        cache.put(ELEMENT_A);
    }

    @Test(expected = IllegalStateException.class)
    public void throwsExceptionIfPutAfterDisposeInIdleCache() {
        cache = new CacheEngineImpl<>(CacheConfiguration.forIdleCache(2, 100));

        cache.dispose();

        cache.put(ELEMENT_A);
    }

    @Test(expected = IllegalStateException.class)
    public void throwsExceptionIfPutAfterDisposeInLiveAndIdleCache() {
        cache = new CacheEngineImpl<>(CacheConfiguration.forLiveAndIdleCache(2, 500, 100));

        cache.dispose();

        cache.put(ELEMENT_A);
    }
}