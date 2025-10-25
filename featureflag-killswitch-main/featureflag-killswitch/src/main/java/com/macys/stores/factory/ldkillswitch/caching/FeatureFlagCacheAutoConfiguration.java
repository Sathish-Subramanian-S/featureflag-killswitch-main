package com.macys.stores.factory.ldkillswitch.caching;

import com.launchdarkly.sdk.server.LDClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the LaunchDarkly feature flag cache.
 * <p>
 * This configuration is activated only when an {@link LDClient} bean is present in the context.
 * It enables the cache properties and creates the {@link FeatureFlagCache} bean.
 */
@Configuration
@ConditionalOnBean(LDClient.class)
@EnableConfigurationProperties(FeatureFlagCacheProperties.class)
public class FeatureFlagCacheAutoConfiguration {

    @Bean
    public FeatureFlagCache featureFlagCache(LDClient ldClient, FeatureFlagCacheProperties cacheProperties) {
        return new FeatureFlagCache(ldClient, cacheProperties);
    }

}