package com.macys.stores.factory.parentapp.config;

import com.macys.stores.factory.parentapp.webclient.config.WebClientProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(WebClientProperties.class)
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Bean
    public Map<String, WebClient> webClients(WebClientProperties properties) {
        return properties.getServices().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> createWebClientForService(entry.getKey(), entry.getValue(), properties)
                ));
    }

    private WebClient createWebClientForService(String serviceName, WebClientProperties.ServiceConfig serviceConfig, WebClientProperties defaults) {
        String baseUrl = Optional.ofNullable(serviceConfig.getBaseUrl()).orElse(defaults.getBaseUrl());
        int connectTimeout = Optional.ofNullable(serviceConfig.getConnectTimeout()).orElse(defaults.getConnectTimeout());
        int readTimeout = Optional.ofNullable(serviceConfig.getReadTimeout()).orElse(defaults.getReadTimeout());

        WebClientProperties.RetryConfig retryConfig = Optional.ofNullable(serviceConfig.getRetry()).orElse(defaults.getRetry());
        WebClientProperties.CircuitBreakerConfig cbConfig = Optional.ofNullable(serviceConfig.getCircuitBreaker()).orElse(defaults.getCircuitBreaker());
        WebClientProperties.ProxyConfig proxyConfig = Optional.ofNullable(serviceConfig.getProxy()).orElse(defaults.getProxy());
        WebClientProperties.LoggingConfig loggingConfig = Optional.ofNullable(serviceConfig.getLogging()).orElse(defaults.getLogging());

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS)));

        if (proxyConfig.isEnabled() && proxyConfig.getHost() != null) {
            httpClient = httpClient.proxy(proxy -> proxy
                    .type(ProxyProvider.Proxy.HTTP)
                    .host(proxyConfig.getHost())
                    .port(proxyConfig.getPort()));
        }

        WebClient.Builder webClientBuilder = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient));

        Map<String, String> finalHeaders = new HashMap<>(defaults.getDefaultHeaders());
        if (serviceConfig.getHeaders() != null) {
            finalHeaders.putAll(serviceConfig.getHeaders());
        }
        finalHeaders.forEach(webClientBuilder::defaultHeader);

        webClientBuilder.filter((request, next) -> {
            Mono<ClientResponse> responseMono = next.exchange(request);

            if (retryConfig.isEnabled()) {
                Retry retry = createRetry(retryConfig, serviceName);
                responseMono = responseMono.transform(RetryOperator.of(retry));
            }

            if (cbConfig.isEnabled()) {
                CircuitBreaker circuitBreaker = createCircuitBreaker(cbConfig, serviceName);
                responseMono = responseMono.transform(CircuitBreakerOperator.of(circuitBreaker));
            }

            return responseMono;
        });

        if (loggingConfig.isEnabled()) {
            webClientBuilder.filter(logRequest())
                          .filter(logResponse());
        }

        return webClientBuilder.build();
    }

    private Retry createRetry(WebClientProperties.RetryConfig retryProps, String serviceName) {
        IntervalFunction intervalFunction = IntervalFunction.ofExponentialBackoff(
                Duration.ofMillis(retryProps.getBackoff().getDelay()),
                retryProps.getBackoff().getMultiplier(),
                Duration.ofMillis(retryProps.getBackoff().getMaxDelay())
        );

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(retryProps.getMaxAttempts())
                .intervalFunction(intervalFunction)
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        return registry.retry(serviceName);
    }

    private CircuitBreaker createCircuitBreaker(WebClientProperties.CircuitBreakerConfig cbProps, String serviceName) {
        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig config = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(cbProps.getFailureRateThreshold())
                .slidingWindowSize(cbProps.getSlidingWindowSize())
                .minimumNumberOfCalls(cbProps.getMinimumNumberOfCalls())
                .waitDurationInOpenState(Duration.ofMillis(cbProps.getWaitDurationInOpenState()))
                .permittedNumberOfCallsInHalfOpenState(cbProps.getPermittedNumberOfCallsInHalfOpenState())
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        return registry.circuitBreaker(serviceName);
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {} {}", clientRequest.method(), clientRequest.url(), clientRequest.headers());
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response Status: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
