package com.eron.challenge.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "movies")
public class MoviesProperties {

    @NotBlank
    private String baseUrl;

    @Min(1)
    private int connectTimeoutMs = 2000;

    @Min(1)
    private int responseTimeoutMs = 4000;

    @Min(0)
    private int maxRetries = 2;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public int getResponseTimeoutMs() { return responseTimeoutMs; }
    public void setResponseTimeoutMs(int responseTimeoutMs) { this.responseTimeoutMs = responseTimeoutMs; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
}
