package com.revenat.simple_cache.cache;

import com.revenat.simple_cache.cache.CacheElement;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class CacheElementTest {
    private final static long CREATION_TIME = 1000;
    private final static long ACCESS_TIME = 1000;
    private static final int KEY = 1;
    private static final String VALUE = "Test";

    private CacheElementStub<Integer, String> element;

    @Test
    public void createsCacheElement() {
        element = new CacheElementStub<>(KEY, VALUE);

        assertThat(element.getKey(), equalTo(KEY));
        assertThat(element.getValue(), equalTo(VALUE));
        assertThat(element.getCreationTime(), equalTo(CREATION_TIME));
    }

    @Test
    public void setsLastAccessTime() {
        element = new CacheElementStub<>(KEY, VALUE);
        assertThat(element.getLastAccessTime(), equalTo(CREATION_TIME));
        element.setCurrentTime(ACCESS_TIME);

        element.setAccessed();

        assertThat(element.getLastAccessTime(), equalTo(ACCESS_TIME));
    }

    class CacheElementStub<K, V> extends CacheElement<K, V> {
        private long currentTime;

        CacheElementStub(K key, V value) {
            super(key, value);
        }

        @Override
        protected long getCurrentTime() {
            return this.currentTime == 0 ? CREATION_TIME : currentTime;
        }

        public void setCurrentTime(long currentTime) {
            this.currentTime = currentTime;
        }
    }
}