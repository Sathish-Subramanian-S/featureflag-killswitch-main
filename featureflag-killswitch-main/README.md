# WebClient Configuration Library

This library provides a pre-configured, resilient, and observable `WebClient` for making HTTP requests in a Spring Boot application. It is designed to be highly configurable via standard `application.properties` or `application.yml` files.

## Features

- **Centralized Configuration**: Manage multiple downstream services from a single properties file.
- **Resilience Out-of-the-Box**: Integrated with Resilience4j for Retries and Circuit Breakers.
- **Sensible Defaults**: Works immediately with built-in defaults, which can be easily overridden.
- **Per-Service Overrides**: Customize timeouts, resilience, headers, and more for each individual service.

---

## Configuration Hierarchy

The library uses a layered approach for configuration. Properties are resolved in the following order of precedence (from highest to lowest):

1.  **Service-Specific Configuration**: Properties defined under `webclient.services.<service-name>.*`. These apply only to a single service.
2.  **Global Configuration**: Properties defined directly under `webclient.*`. These apply to all services unless overridden by a service-specific setting.
3.  **Library Defaults**: The default values hard-coded in the library's `WebClientProperties.java` class. These are used if no global or service-specific value is provided.

---

## How to Configure

To use the library, define your downstream services under the `webclient.services` prefix in your `application.properties`. Each service is identified by a unique key (e.g., `locationService`, `productApi`).

**Example `application.properties`:**

```properties
# Define a service named 'locationService'
webclient.services.locationService.baseUrl=https://api.locations.com
webclient.services.locationService.path=/v1/locations/{id}

# Define another service with a custom timeout
webclient.services.productApi.baseUrl=https://api.products.com
webclient.services.productApi.path=/v2/products/{sku}
webclient.services.productApi.read-timeout=5000
```

---

## Configuration Properties

The following tables document all available properties that can be configured in your `application.properties` file.

### 🔹 1. Base Configuration

| Property                    | Default Value              | Description                                                  |
| :-------------------------- | :------------------------- | :----------------------------------------------------------- |
| `webclient.base-url`        | `http://localhost:8080`    | The default base URL if not set at the service level.        |
| `webclient.default-headers.*` | (none)                     | A map of default headers to apply to all requests.           |
| `webclient.connect-timeout` | `5000`                     | Default connection timeout in milliseconds.                  |
| `webclient.read-timeout`    | `5000`                     | Default read timeout in milliseconds.                        |

### 🔹 2. Retry Configuration (Resilience4j)

**Prefix:** `webclient.retry.*`

| Property             | Default Value | Description                                                          |
| :------------------- | :------------ | :------------------------------------------------------------------- |
| `enabled`            | `true`        | Whether to enable the retry mechanism.                               |
| `max-attempts`       | `3`           | The maximum number of attempts (including the first call).           |
| `backoff.delay`      | `1000`        | The initial wait duration in ms before the first retry.              |
| `backoff.max-delay`  | `5000`        | The maximum wait duration in ms between retries.                     |
| `backoff.multiplier` | `2.0`         | The multiplier to apply to the delay for subsequent retries (for exponential backoff). |

### 🔹 3. Circuit Breaker Configuration (Resilience4j)

**Prefix:** `webclient.circuit-breaker.*`

| Property                                     | Default Value | Description                                                          |
| :------------------------------------------- | :------------ | :------------------------------------------------------------------- |
| `enabled`                                    | `true`        | Whether to enable the circuit breaker.                               |
| `failure-rate-threshold`                     | `50`          | The failure rate percentage that will open the circuit.              |
| `sliding-window-size`                        | `100`         | The number of recent calls to consider for calculating the failure rate. |
| `minimum-number-of-calls`                    | `10`          | The minimum number of calls required before the failure rate is calculated. |
| `wait-duration-in-open-state`                | `30000`       | The duration in ms the circuit will stay open before transitioning to half-open. |
| `permitted-number-of-calls-in-half-open-state` | `5`           | The number of permitted calls when the circuit is half-open.         |

### 🔹 4. Fallback Configuration

**Prefix:** `webclient.fallback.*`

| Property          | Default Value                                        | Description                                                          |
| :---------------- | :--------------------------------------------------- | :------------------------------------------------------------------- |
| `enabled`         | `true`                                               | Whether to enable automatic fallback responses. *(Note: Implementation is in the consuming service)*. |
| `response-body`   | `{"message":"Service unavailable..."}`               | The default JSON string to return in a fallback.                     |
| `status-code`     | `503`                                                | The default HTTP status code for a fallback response.                |

### 🔹 5. Method-Specific Timeout

**Prefix:** `webclient.timeout.*`

| Property | Default Value | Description                                  |
| :------- | :------------ | :------------------------------------------- |
| `get`    | `3000`        | Default read timeout in ms for GET requests. |
| `post`   | `5000`        | Default read timeout in ms for POST requests.|
| `put`    | `5000`        | Default read timeout in ms for PUT requests. |
| `delete` | `3000`        | Default read timeout in ms for DELETE requests.|

### 🔹 6. Logging and Debugging

**Prefix:** `webclient.logging.*`

| Property  | Default Value | Description                                          |
| :-------- | :------------ | :--------------------------------------------------- |
| `enabled` | `true`        | Whether to enable basic request/response logging.    |
| `level`   | `DEBUG`       | The log level for the output (e.g., `DEBUG`, `TRACE`). |

### 🔹 7. Proxy Configuration

**Prefix:** `webclient.proxy.*`

| Property  | Default Value | Description                             |
| :-------- | :------------ | :-------------------------------------- |
| `enabled` | `false`       | Whether to route requests through a proxy. |
| `host`    | (none)        | The hostname of the proxy server.       |
| `port`    | `8080`        | The port of the proxy server.           |

---

## Complete Configuration Example

This example shows how to set global defaults and override them for specific services.

```properties
#################################################
# 1. GLOBAL WEBCLIENT DEFAULTS
#################################################
# Set a global read timeout for all services
webclient.read-timeout=8000

# Apply a default header to all outgoing requests
webclient.default-headers.X-Application-Name=MyAwesomeApp

# Adjust the global default for retry attempts
webclient.retry.max-attempts=5

#################################################
# 2. SERVICE-SPECIFIC CONFIGURATIONS
#################################################

# --- Configuration for 'locationService' ---
# This service will use the global defaults (8s timeout, 5 retries).
webclient.services.locationService.baseUrl=https://api.locations.com
webclient.services.locationService.path=/v1/locations/{id}

# --- Configuration for 'paymentService' ---
# This service is critical and requires specific, more aggressive settings.
webclient.services.paymentService.baseUrl=https://api.payments.com
webclient.services.paymentService.path=/v2/charge

# Override the global read timeout for this service only
webclient.services.paymentService.read-timeout=2000

# Override the global retry settings for this service only
webclient.services.paymentService.retry.max-attempts=2

# Add a service-specific header (will be merged with global headers)
webclient.services.paymentService.headers.X-Api-Version=2.1

---

## 🔹 8. LaunchDarkly Integration

The library includes a `FeatureFlagService` to gate service calls based on LaunchDarkly feature flags. This allows you to dynamically enable or disable functionality in production.

### How to Use

When making a call with `WebClientService`, you can provide a `FeatureFlagContext`. If the context is provided, the library will check the specified flag. If the flag is disabled, the service call will be skipped, and the fallback logic will be executed immediately.

**Example in a Parent Application Service:**

```java
public Mono<Location> getGatedLocationById(String locationId, String userSessionId) {
    // Define the context for the feature flag check
    FeatureFlagContext flagContext = FeatureFlagContext.builder()
            .flagKey("enable-new-location-service") // The flag key in LaunchDarkly
            .userKey(userSessionId) // A unique identifier for the user
            .channel("MCOM") // Optional: override the default channel
            .build();

    return webClientService.get(
        "locationService",
        Location.class,
        flagContext, // Pass the context here
        error -> Mono.empty(),
        locationId
    );
}
```

### LaunchDarkly Configuration Properties

**Prefix:** `launchdarkly.*`

| Property          | Default Value   | Description                                                                  |
| :---------------- | :-------------- | :--------------------------------------------------------------------------- |
| `sdk-key`         | (none)          | **Required.** The SDK key for your LaunchDarkly environment.                 |
| `offline-mode`    | `false`         | Set to `true` to prevent the SDK from connecting to LaunchDarkly.            |
| `default-channel` | `MCOM`          | The default channel to use for user targeting if not specified in the call.  |
| `default-store`   | `default-store` | The default store ID to use for user targeting if not specified in the call. |
```
