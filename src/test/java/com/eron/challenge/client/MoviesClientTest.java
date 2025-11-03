package com.eron.challenge.client;

import com.eron.challenge.config.MoviesProperties;
import com.eron.challenge.model.external.ApiMoviesPageResponse;
import com.eron.challenge.model.external.Movie;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MoviesClientTest {
    private MockWebServer server;
    private MoviesClient client;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setup() throws IOException {
        server = new MockWebServer();
        server.start();

        MoviesProperties props = new MoviesProperties();
        props.setBaseUrl(server.url("/").toString());
        props.setConnectTimeoutMs(2000);
        props.setResponseTimeoutMs(4000);
        props.setMaxRetries(2);

        WebClient webClient = WebClient.builder().baseUrl(props.getBaseUrl()).build();
        client = new MoviesClient(webClient, props);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void testFetchAllMoviesPaginatesAgregates() throws Exception {
        // page 1 (total_pages=2)
        server.enqueue(json200(new ApiMoviesPageResponse(1, 10, 3, 2, List.of(
                new Movie("M1","2011","PG", "2011-01-01","90 min", "Drama", "A", "W", "C")
        ))));
        // page 2
        server.enqueue(json200(new ApiMoviesPageResponse(2, 10, 3, 2, List.of(
                new Movie("M2", "2012", "PG", "2012-01-01", "100 min", "Horror", "B", "W", "C"),
                new Movie("M3", "2013", "PG", "2013-01-01", "130 min", "Sci-Fi", "C", "W", "C")
        ))));

        Flux<Movie> flux = client.fetchAllMovies();

        StepVerifier.create(flux)
                .expectNextCount(3)
                .verifyComplete();

        assertThat(server.getRequestCount()).isEqualTo(2);
        assertThat(server.takeRequest().getPath()).isEqualTo("/api/movies/search?page=1");
        assertThat(server.takeRequest().getPath()).isEqualTo("/api/movies/search?page=2");
    }

    @Test
    public void testFetchPagePropagatesServerError() throws JsonProcessingException {
        server.enqueue(json500(Map.of("error","boom")));
        StepVerifier.create(client.fetchPage(1))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(WebClientResponseException.class);
                    var wcre = (WebClientResponseException)ex;
                    assertThat(wcre.getStatusCode().value()).isEqualTo(500);
                })
                .verify();

        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    private MockResponse json200(Object body) throws JsonProcessingException {
        return new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(om.writeValueAsString(body));
    }
    private MockResponse json500(Object body) throws JsonProcessingException {
        return new MockResponse()
                .setResponseCode(500)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(om.writeValueAsString(body));
    }

}
