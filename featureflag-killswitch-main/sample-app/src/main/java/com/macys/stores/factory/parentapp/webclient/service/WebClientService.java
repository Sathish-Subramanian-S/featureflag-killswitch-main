package com.macys.stores.factory.parentapp.webclient.service;

import com.macys.stores.factory.ldkillswitch.model.FeatureFlagContext;
import com.macys.stores.factory.parentapp.webclient.config.WebClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;

@Service
public class WebClientService {

    private static final Logger log = LoggerFactory.getLogger(WebClientService.class);

    private final WebClientProperties webClientProperties;
    private final Map<String, WebClient> webClients;
    private final com.macys.stores.factory.ldkillswitch.service.FeatureFlagService featureFlagService;

    public WebClientService(WebClientProperties webClientProperties, Map<String, WebClient> webClients, com.macys.stores.factory.ldkillswitch.service.FeatureFlagService featureFlagService) {
        this.webClientProperties = webClientProperties;
        this.webClients = webClients;
        this.featureFlagService = featureFlagService;
    }

    private WebClient getClientForService(String serviceName) {
        WebClient client = webClients.get(serviceName);
        if (client == null) {
            log.error("No WebClient configured for service: {}", serviceName);
            throw new IllegalArgumentException("No WebClient configuration found for service: " + serviceName);
        }
        return client;
    }

    private String getPathForService(String serviceName) {
        return webClientProperties.getServices().getOrDefault(serviceName, new WebClientProperties.ServiceConfig()).getPath();
    }

    public <T> Mono<T> get(String serviceName, Class<T> responseType, com.macys.stores.factory.ldkillswitch.model.FeatureFlagContext featureFlagContext, Function<Throwable, Mono<T>> fallback, Object... uriVariables) {
        if (featureFlagContext != null) {
            boolean isValid = featureFlagService.isRequestContextValid(featureFlagContext);
            if (!isValid) {
                log.warn("Request context is invalid. Skipping service call to '{}'. Context: {}", serviceName, featureFlagContext);
                return fallback.apply(new WebClientResponseException("Invalid request context", 400, "Bad Request", null, null, null));
            }
        }

        String path = getPathForService(serviceName);
        if (path == null) {
            throw new IllegalArgumentException("No path configured for service: " + serviceName);
        }
        return getClientForService(serviceName)
                .get()
                .uri(path, uriVariables)
                .retrieve()
                .bodyToMono(responseType)
                .doOnError(error -> log.error("GET request to service '{}' with path '{}' failed", serviceName, path, error))
                .onErrorResume(fallback);
    }

    public <T, R> Mono<R> post(String serviceName, Class<R> responseType, T requestBody, FeatureFlagContext featureFlagContext, Function<Throwable, Mono<R>> fallback, Object... uriVariables) {
        if (featureFlagContext != null) {
            boolean isValid = featureFlagService.isRequestContextValid(featureFlagContext);
            if (!isValid) {
                log.warn("Request context is invalid. Skipping service call to '{}'. Context: {}", serviceName, featureFlagContext);
                return fallback.apply(new WebClientResponseException("Invalid request context", 400, "Bad Request", null, null, null));
            }
        }

        String path = getPathForService(serviceName);
        if (path == null) {
            throw new IllegalArgumentException("No path configured for service: " + serviceName);
        }
        return getClientForService(serviceName)
                .post()
                .uri(path, uriVariables)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .doOnError(error -> log.error("POST request to service '{}' with path '{}' failed", serviceName, path, error))
                .onErrorResume(fallback);
    }
}
