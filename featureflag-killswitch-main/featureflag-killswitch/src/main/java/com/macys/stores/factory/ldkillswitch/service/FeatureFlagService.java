package com.macys.stores.factory.ldkillswitch.service;

import com.macys.stores.factory.ldkillswitch.caching.FeatureFlagCache;
import com.macys.stores.factory.ldkillswitch.config.LaunchDarklyProperties;
import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.LDClient;
import com.macys.stores.factory.ldkillswitch.model.FeatureFlagContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final LDClient ldClient;
    private final LaunchDarklyProperties ldProperties;
    private final FeatureFlagCache featureFlagCache;
    private static final Logger log = LoggerFactory.getLogger(FeatureFlagService.class);

    public boolean isFeatureEnabled(String flagKey, String userKey, boolean defaultValue) {
        LDUser user = new LDUser.Builder(userKey).build();
//        return featureFlagCache.getBooleanFlagValue(flagKey, user, false);
        return ldClient.boolVariation(flagKey, user, defaultValue);
    }

    public boolean isFeatureEnabledForStores(String flagKey, String userKey, boolean defaultValue, String sourceChannel, String sourceSystem, String storeNumber) {
        LDUser user = new LDUser.Builder(userKey) .custom("channel", LDValue.of(sourceChannel))
                            .custom("sourceSystem", LDValue.of(sourceSystem))
                            .custom("storenumber", LDValue.of(storeNumber)).build();
        return ldClient.boolVariation(flagKey, user, defaultValue);
    }

    public boolean isRequestContextValid(FeatureFlagContext context) {
        return isFeatureEnabled(context.getFlagKey(), context.getUserKey(), false);
    }

    public boolean isFeatureEnabledInConfig(String flagKey, String userKey, LDValue defaultValue, String sourceChannel, String sourceSystem, String storeNumber) {
        LDUser user = new LDUser.Builder(userKey) .custom("channel", LDValue.of(sourceChannel))
                .custom("sourceSystem", LDValue.of(sourceSystem))
                .custom("storenumber", LDValue.of(storeNumber)).build();

        LDValue flagValue = featureFlagCache.getJsonFlagValue(flagKey, user, LDValue.of("{}"));

        LDValue channels = flagValue.get("supportedChannels");
        LDValue systems = flagValue.get("supportedSourceSystems");
        LDValue stores = flagValue.get("supportedStores");

        boolean isAllowed = ldArrayContains(channels, sourceChannel) &&
                ldArrayContains(systems, sourceSystem) &&
                ldArrayContains(stores, storeNumber);

        return isAllowed;
    }

    private static boolean ldArrayContains(LDValue array, String value) {
        if (array == null) return false;
        for (LDValue item : array.values()) {
            if (item.stringValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAttributeValid(String attributeName, @Nullable String value, List<String> supportedValues) {
        if (supportedValues.isEmpty()) {
            log.warn("Validation for '{}' is active, but the supported list is empty. Denying request.", attributeName);
            return false;
        }

        if (value == null || !supportedValues.contains(value)) {
            log.warn("Invalid request context: '{}' value '{}' is not in the supported list: {}", attributeName, value, supportedValues);
            return false;
        }

        return true;
    }
}
