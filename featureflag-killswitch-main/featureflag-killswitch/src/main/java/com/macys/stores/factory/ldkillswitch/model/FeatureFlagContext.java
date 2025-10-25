package com.macys.stores.factory.ldkillswitch.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
@Builder
public class FeatureFlagContext {
    private final String flagKey;
    private final String userKey;
    @Nullable private final String channel;
    @Nullable private final String storeId;
    @Nullable private final String sourceSystem;
}