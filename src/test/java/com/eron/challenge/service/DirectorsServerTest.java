package com.eron.challenge.service;

import com.eron.challenge.client.MoviesClient;
import com.eron.challenge.model.external.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

public class DirectorsServerTest {

    private MoviesClient moviesClient;
    private DirectorsService service;

    @BeforeEach
    void setUp() {
        moviesClient = Mockito.mock(MoviesClient.class);
        service = new DirectorsService(moviesClient);
    }

    @Test
    public void testReturnsDirectorsStrictlyAboveThresholdSorted() {
        Flux<Movie> movies = Flux.just(
                movie("Movie 1", "Martin Scorsese"),
                movie("Movie 2", "Martin Scorsese"),
                movie("Movie 3", "Martin Scorsese"),
                movie("Movie 4", "Martin Scorsese"),
                movie("Movie 5", "Martin Scorsese"),
                movie("Another 1", "Woody Allen"),
                movie("Another 2", "Woody Allen"),
                movie("Another 3", "Woody Allen"),
                movie("Another 4", "Woody Allen"),
                movie("Another 5", "Woody Allen"),
                movie("Other", "Christopher Nolan")
        );
        when(moviesClient.fetchAllMovies()).thenReturn(movies);

        StepVerifier.create(service.getDirectorsAboveThreshold(4))
                .expectNextMatches(resp -> {
                    List<String> expected = List.of("Martin Scorsese", "Woody Allen");
                    return expected.equals(resp.directors());
                })
                .verifyComplete();
    }

    @Test
    public void testFiltersEmptyOrNullDirectors() {
        Flux<Movie> movies = Flux.just(
                movie("M1", ""),
                movie("M2", null),
                movie("M3", "N/A"),
                movie("M4", "N/ A"),
                movie("M5", "A")
        );
        when(moviesClient.fetchAllMovies()).thenReturn(movies);

        StepVerifier.create(service.getDirectorsAboveThreshold(1))
                .expectNextMatches(resp -> resp.directors().isEmpty())
                .verifyComplete();
    }

    private Movie movie(String title, String director) {
        return new Movie(
                title, "2012", "PG-13", "2012-01-01", "130 min", "Horror", director, "Writer", "Actor"
        );
    }
}
