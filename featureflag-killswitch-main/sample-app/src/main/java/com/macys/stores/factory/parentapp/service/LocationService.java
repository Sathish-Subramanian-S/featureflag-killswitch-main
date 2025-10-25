package com.macys.stores.factory.parentapp.service;

import com.macys.stores.factory.ldkillswitch.model.FeatureFlagContext;
import com.macys.stores.factory.parentapp.model.Location;
import com.macys.stores.factory.parentapp.webclient.service.WebClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class LocationService {

    private final WebClientService webClientService;

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    public LocationService(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    public Mono<Location> getLocationById(String locationId, String userKey) {
        FeatureFlagContext flagContext = FeatureFlagContext.builder()
                .flagKey("enable-location-service-call")
                .userKey(userKey)
                .channel("MCOM")
                .storeId("Site")
                .sourceSystem("Site")
                .build();

        return webClientService.get("locationService", Location.class, flagContext, error -> {
            if (error instanceof WebClientResponseException.Forbidden && error.getMessage().contains("Feature flag disabled")) {
                log.debug("Fallback triggered because feature flag '{}' is disabled.", flagContext.getFlagKey());
            } else if (error instanceof WebClientResponseException.NotFound) {
                log.warn("Location with ID '{}' not found (404).", locationId);
            } else {
                log.error("Fallback for getLocationById('{}') triggered due to error: {}", locationId, error.getMessage());
            }
            return Mono.empty();
        }, locationId);
    }

    public Mono<String> getLocationByIdAsString(String locationId, String userKey) {
        FeatureFlagContext flagContext = FeatureFlagContext.builder()
                .flagKey("locationServiceEnabled")
                .userKey(userKey)
                .channel("MCOM")
                .storeId("Site")
                .sourceSystem("Site")
                .build();

        return webClientService.get("locationService", String.class, flagContext, error -> {
            if (error instanceof WebClientResponseException.Forbidden && error.getMessage().contains("Feature flag disabled")) {
                log.debug("Fallback triggered because feature flag '{}' is disabled.", flagContext.getFlagKey());
            } else if (error instanceof WebClientResponseException.NotFound) {
                log.warn("Location with ID '{}' not found (404).", locationId);
            } else {
                log.error("Fallback for getLocationByIdAsString('{}') triggered due to error: {}", locationId, error.getMessage());
            }
            return Mono.empty();
        }, locationId);
    }
}
