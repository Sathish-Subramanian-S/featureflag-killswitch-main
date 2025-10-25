package com.macys.stores.factory.ldkillswitch.caching;


import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.shaded.com.google.common.cache.Cache;
import com.launchdarkly.shaded.com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * A caching service for LaunchDarkly feature flag evaluations.
 * This service uses an in-memory Guava Cache to store flag values for a specific user and flag key,
 * reducing the number of direct calls to the LaunchDarkly SDK.
 */
@Slf4j
@Component
public class FeatureFlagCache {

    private final LDClient ldClient;
    private final Cache<String, LDValue> flagCache;
//    private final Cache<String, Boolean> booleanFlagCache;

//    @Value("${ldkillswitch.cache.expiration.time}")
//    private long cacheExpirationTime;

    /**
     * Constructs the cache service.
     *
     * @param ldClient                 The LaunchDarkly client instance.
     * @param cacheProperties          The configuration for the cache.
     */
    public FeatureFlagCache(LDClient ldClient, FeatureFlagCacheProperties cacheProperties) {
        this.ldClient = ldClient;
        this.flagCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheProperties.getExpireAfterWriteMinutes(), TimeUnit.MINUTES)
                .maximumSize(cacheProperties.getMaximumSize())
                .build();
//        this.booleanFlagCache = booleanFlagCache;
        log.info("Initialized FeatureFlagCache with {} minutes TTL and a max size of {}.",
                cacheProperties.getExpireAfterWriteMinutes(), cacheProperties.getMaximumSize());
    }

    /**
     * Retrieves a JSON feature flag value for a given user.
     * It first checks the local cache. If the value is not present, it fetches it from LaunchDarkly,
     * caches it, and then returns it.
     *
     * @param flagKey      The key of the feature flag.
     * @param user         The user context for the evaluation.
     * @param defaultValue The default value to return if the flag cannot be evaluated.
     * @return The evaluated {@link LDValue}, either from the cache or from LaunchDarkly.
     */
    public LDValue getJsonFlagValue(String flagKey, LDUser user, LDValue defaultValue) {
        String cacheKey = flagKey + ":" + user.getKey();
        LDValue cachedValue = flagCache.getIfPresent(cacheKey);
        if (cachedValue != null) {
            log.debug("Cache hit for flag '{}' and user '{}'", flagKey, user.getKey());
            return cachedValue;
        }

        log.debug("Cache miss for flag '{}' and user '{}'. Fetching from LaunchDarkly.", flagKey, user.getKey());
        LDValue freshValue = ldClient.jsonValueVariation(flagKey, user, defaultValue);
        flagCache.put(cacheKey, freshValue);
        return freshValue;
    }

//    /**
//     * Retrieves a JSON feature flag value for a given user.
//     * It first checks the local cache. If the value is not present, it fetches it from LaunchDarkly,
//     * caches it, and then returns it.
//     *
//     * @param flagKey      The key of the feature flag.
//     * @param user         The user context for the evaluation.
//     * @param defaultValue The default value to return if the flag cannot be evaluated.
//     * @return The evaluated {@link LDValue}, either from the cache or from LaunchDarkly.
//     */
//    public boolean getBooleanFlagValue(String flagKey, LDUser user, boolean defaultValue) {
//        String cacheKey = flagKey + ":" + user.getKey();
//        if(booleanFlagCache.getIfPresent(cacheKey))
//        {
//            return booleanFlagCache.getIfPresent(cacheKey);
//        }
//
//        log.debug("Cache miss for flag '{}' and user '{}'. Fetching from LaunchDarkly.", flagKey, user.getKey());
//        boolean freshValue =  ldClient.boolVariation(flagKey, user, defaultValue);
//        booleanFlagCache.put(cacheKey, freshValue);
//        return freshValue;
//    }
}