package com.eron.challenge.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(MoviesProperties.class)
public class HttpClientConfig {

    @Bean
    WebClient moviesWebClient(WebClient.Builder builder, MoviesProperties props) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(props.getResponseTimeoutMs()));

        return builder
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        var log = org.slf4j.LoggerFactory.getLogger("com.eron.challenge.http");
        return ExchangeFilterFunction.ofRequestProcessor(request ->
            reactor.core.publisher.Mono.just(request)
                    .doOnNext( r -> log.debug("HTTP {} {}", r.method(), r.url())));
    }

    private ExchangeFilterFunction logResponse() {
        var log = org.slf4j.LoggerFactory.getLogger("com.eron.challenge.http");
        return ExchangeFilterFunction.ofResponseProcessor(response ->
            reactor.core.publisher.Mono.just(response)
                    .doOnNext(r -> log.debug("HTTP status {}", r.statusCode())));
    }
}
