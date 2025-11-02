package com.eron.challenge.service;

import com.eron.challenge.client.MoviesClient;
import com.eron.challenge.model.api.DirectorsResponse;
import com.eron.challenge.model.external.Movie;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.function.Function;

@Service
public class DirectorsService {

    private final MoviesClient moviesClient;

    public DirectorsService(MoviesClient moviesClient) { this.moviesClient = moviesClient; }

    public Mono<DirectorsResponse> getDirectorsAboveThreshold(int threshold) {
        return moviesClient.fetchAllMovies()
                .handle((Movie m, SynchronousSink<String> sink) -> {
                    String d = m.director();
                    if (d == null) return;
                    d = d.trim();
                    String noSpaces = d.replace("\s+","");
                    if(!d.isBlank() && !"N/A".equalsIgnoreCase(noSpaces)){ sink.next(d); }
                })
                .groupBy(Function.identity())
                .flatMap(group -> group.count().map(cnt -> Tuples.of(group.key(), cnt)))
                .filter(t -> t.getT2() > threshold)
                .map(Tuple2::getT1)
                .sort()
                .collectList()
                .map(DirectorsResponse::new);
    }

}
