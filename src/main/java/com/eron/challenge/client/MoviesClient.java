package com.eron.challenge.client;

import com.eron.challenge.config.MoviesProperties;
import com.eron.challenge.model.external.ApiMoviesPageResponse;
import com.eron.challenge.model.external.Movie;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

@Component
public class MoviesClient {

    private final WebClient webClient;
    private final MoviesProperties props;

    public MoviesClient(WebClient webClient, MoviesProperties props) {
        this.webClient = webClient;
        this.props = props;
    }

    @CircuitBreaker(name="movies")
    @Retry(name="movies")
    public Mono<ApiMoviesPageResponse> fetchPage(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/movies/search")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.createException().flatMap(Mono::error))
                .bodyToMono(ApiMoviesPageResponse.class);
    }

    public Flux<Movie> fetchAllMovies() {
        return fetchPage(1)
                .flatMapMany(firstPage -> {
                    int totalPages = Math.max(1, firstPage.totalPages());
                    Flux<Movie> firstData = Flux.fromIterable(firstPage.data());
                    if (totalPages == 1) return firstData;

                    int remainingCount = totalPages - 1;
                    Flux<Movie> rest = Flux
                            .range(2, remainingCount)
                            .flatMap(this::fetchPage, 4)
                            .flatMapIterable(ApiMoviesPageResponse::data);
                    return Flux.concat(firstData, rest);
                });
    }

    private Predicate<Throwable> transientError() {
        return ex -> {
            if (ex instanceof ConnectException) return true;
            if (ex instanceof TimeoutException) return true;
            if (ex instanceof ReadTimeoutException) return true;
            if (ex instanceof WebClientRequestException) return true;
            if (ex instanceof WebClientResponseException wcre) {
                return wcre.getStatusCode().is5xxServerError();
            }
            return false;
        };
    }
}
