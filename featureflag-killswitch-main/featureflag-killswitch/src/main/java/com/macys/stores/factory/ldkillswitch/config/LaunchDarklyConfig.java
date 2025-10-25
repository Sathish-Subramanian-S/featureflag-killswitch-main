package com.macys.stores.factory.ldkillswitch.config;

import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(com.macys.stores.factory.ldkillswitch.config.LaunchDarklyProperties.class)
public class LaunchDarklyConfig {

    private static final Logger log = LoggerFactory.getLogger(LaunchDarklyConfig.class);

    @Value("${launchdarkly.sdk-key}")
    private String ldKey;

    @Bean
    @ConditionalOnMissingBean
    public LDClient ldClient(com.macys.stores.factory.ldkillswitch.config.LaunchDarklyProperties properties) {
        String sdkKey = properties.getSdkKey();
        if (sdkKey == null || sdkKey.trim().isEmpty()) {
            return new LDClient(ldKey, new LDConfig.Builder().build());
        }

        LDConfig config = new LDConfig.Builder().build();

        log.info("Initializing LaunchDarkly client. Offline mode: {}", properties.isOfflineMode());
        return new LDClient(sdkKey, config);
    }
}
