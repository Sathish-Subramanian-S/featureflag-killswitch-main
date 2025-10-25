package com.macys.stores.factory.ldkillswitch.caching;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * Configuration properties for the LaunchDarkly feature flag cache.
 * <p>
 * Allows configuring cache behavior via application properties.
 * Example `application.yml` configuration:
 * <pre>
 * launchdarkly.cache:
 *   expire-after-write-minutes: 10
 *   maximum-size: 1000
 * </pre>
 */
@ConfigurationProperties(prefix = "ldkillswitch.cache")
@Data
public class FeatureFlagCacheProperties {

    /**
     * The duration in minutes after which a cache entry should be automatically removed after being created or replaced.
     */
    private long expireAfterWriteMinutes = 10;

    /** The maximum number of entries the cache may contain. */
    private long maximumSize = 1000;
}