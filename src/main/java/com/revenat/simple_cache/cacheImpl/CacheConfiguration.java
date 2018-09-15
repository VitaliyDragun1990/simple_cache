package com.revenat.simple_cache.cacheImpl;

public class CacheConfiguration {
    private final int maxElements;
    private final long lifeTimeMs;
    private final long idleTimeMs;
    private final boolean isEternal;


    public static CacheConfiguration forEternalCache(int maxElements) {
        return new CacheConfiguration(maxElements, 0, 0, true);
    }

    public static CacheConfiguration forIdleCache(int maxElements, long idleTimeMs) {
        return new CacheConfiguration(maxElements, 0, idleTimeMs, false);
    }

    public static CacheConfiguration forLiveCache(int maxElements, long lifeTimeMs) {
        return new CacheConfiguration(maxElements, lifeTimeMs, 0, false);
    }

    public static CacheConfiguration forLiveAndIdleCache(int maxElements, long lifeTimeMs, long idleTimeMs) {
        return new CacheConfiguration(maxElements, lifeTimeMs, idleTimeMs, false);
    }

    private CacheConfiguration(int maxElements, long lifeTimeMs, long idleTimeMs, boolean isEternal) {
        this.maxElements = maxElements;
        this.lifeTimeMs = lifeTimeMs;
        this.idleTimeMs = idleTimeMs;
        this.isEternal = isEternal;
    }

    public int getMaxElements() {
        return maxElements;
    }

    public long getLifeTimeMs() {
        return lifeTimeMs;
    }

    public long getIdleTimeMs() {
        return idleTimeMs;
    }

    public boolean isEternal() {
        return isEternal;
    }
}
