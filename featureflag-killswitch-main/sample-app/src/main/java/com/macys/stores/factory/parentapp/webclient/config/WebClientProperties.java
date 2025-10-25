package com.macys.stores.factory.parentapp.webclient.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "webclient")
@Getter
@Setter
public class WebClientProperties {

    private String baseUrl = "http://localhost:8080";
    private Map<String, String> defaultHeaders = new HashMap<>();
    private int connectTimeout = 5000;
    private int readTimeout = 5000;

    private RetryConfig retry = new RetryConfig();
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();
    private FallbackConfig fallback = new FallbackConfig();
    private TimeoutConfig timeout = new TimeoutConfig();
    private LoggingConfig logging = new LoggingConfig();
    private ProxyConfig proxy = new ProxyConfig();

    private Map<String, ServiceConfig> services = new HashMap<>();

    @Getter
    @Setter
    public static class ServiceConfig {
        @Nullable private String baseUrl;
        @Nullable private String path;
        @Nullable private Integer connectTimeout;
        @Nullable private Integer readTimeout;
        @Nullable private Map<String, String> headers;

        @Nullable private RetryConfig retry;
        @Nullable private CircuitBreakerConfig circuitBreaker;
        @Nullable private FallbackConfig fallback;
        @Nullable private TimeoutConfig timeout;
        @Nullable private LoggingConfig logging;
        @Nullable private ProxyConfig proxy;

    }

    @Getter
    @Setter
    public static class RetryConfig {
        private boolean enabled = true;
        private int maxAttempts = 3;
        private BackoffConfig backoff = new BackoffConfig();

        @Getter
        @Setter
        public static class BackoffConfig {
            private long delay = 1000;
            private long maxDelay = 5000;
            private double multiplier = 2.0;
        }
    }

    @Getter
    @Setter
    public static class CircuitBreakerConfig {
        private boolean enabled = true;
        private float failureRateThreshold = 50;
        private int slidingWindowSize = 100;
        private int minimumNumberOfCalls = 10;
        private long waitDurationInOpenState = 30000;
        private int permittedNumberOfCallsInHalfOpenState = 5;
    }

    @Getter
    @Setter
    public static class FallbackConfig {
        private boolean enabled = true;
        private String responseBody = "{\"message\":\"Service unavailable. Please try again later.\"}";
        private int statusCode = 503;
    }

    @Getter
    @Setter
    public static class TimeoutConfig {
        private int get = 3000;
        private int post = 5000;
        private int put = 5000;
        private int delete = 3000;
    }

    @Getter
    @Setter
    public static class LoggingConfig {
        private boolean enabled = true;
        private String level = "DEBUG";
    }

    @Getter
    @Setter
    public static class ProxyConfig {
        private boolean enabled = false;
        private String host;
        private int port = 8080;
    }
}
