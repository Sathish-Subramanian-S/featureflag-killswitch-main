package com.macys.stores.factory.ldkillswitch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "launchdarkly")
@Getter
@Setter
public class LaunchDarklyProperties {

    private String sdkKey;

    private boolean offlineMode = false;

    private String validationFlagKey = "enable-request-context-validation";

    private List<String> supportedChannels = new ArrayList<>();

    private List<String> supportedSourceSystems = new ArrayList<>();

    private List<String> supportedStores = new ArrayList<>();
}