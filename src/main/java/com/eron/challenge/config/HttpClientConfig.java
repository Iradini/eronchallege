package com.eron.challenge.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(MoviesProperties.class)
public class HttpClientConfig {

    @Bean
    WebClient moviesWebClient(WebClient.Builder builder, MoviesProperties props) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(props.getResponseTimeoutMs()))
                .doOnConnected(connection ->
                        connection.addHandlerLast(new ReadTimeoutHandler(props.getResponseTimeoutMs(),
                                TimeUnit.MILLISECONDS)));

        return builder
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            return reactor.core.publisher.Mono.fromRunnable(() ->
                    org.slf4j.LoggerFactory.getLogger("com.eron.challenge.http")
                            .debug("HTTP {} {}", request.method(), request.url()));
        }).andThen(ExchangeFilterFunction.ofRequestProcessor(reactor.core.publisher.Mono::just));
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            return reactor.core.publisher.Mono.fromRunnable(() ->
                    org.slf4j.LoggerFactory.getLogger("com.eron.challenge.http")
                            .debug("HTTP status {}", response.statusCode()));
        }).andThen(ExchangeFilterFunction.ofResponseProcessor(reactor.core.publisher.Mono::just));
    }
}
