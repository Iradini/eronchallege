package com.eron.challenge.client;

import com.eron.challenge.config.MoviesProperties;
import com.eron.challenge.model.external.ApiMoviesPageResponse;
import com.eron.challenge.model.external.Movie;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.function.Predicate;

@Component
public class MoviesClient {

    private final WebClient webClient;
    private final MoviesProperties props;

    public MoviesClient(WebClient webClient, MoviesProperties props) {
        this.webClient = webClient;
        this.props = props;
    }

    public Mono<ApiMoviesPageResponse> fetchPage(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/movies/search")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.createException().flatMap(Mono::error))
                .bodyToMono(ApiMoviesPageResponse.class)
                .retryWhen(retrySpec());
    }

    public Flux<Movie> fetchAllMovies() {
        return fetchPage(1)
                .flatMapMany(firstPage -> {
                    int totalPages = Math.max(1, firstPage.totalPages());
                    return Flux
                            .range(1, totalPages)
                            .flatMap(this::fetchPage, 4)
                            .flatMapIterable(ApiMoviesPageResponse::data);
                });
    }

    private Retry retrySpec() {
        int maxAttempts = Math.max(0, props.getMaxRetries());
        return Retry
                .backoff(maxAttempts, Duration.ofMillis(300))
                .filter(transientError())
                .maxBackoff(Duration.ofSeconds(2));
    }

    private Predicate<Throwable> transientError() {
        return ex -> {
            if (ex instanceof ConnectException) return true;
            if (ex instanceof WebClientResponseException wcre) {
                return wcre.getStatusCode().is5xxServerError();
            }
            return false;
        };
    }
}
