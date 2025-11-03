package com.eron.challenge.service;

import com.eron.challenge.client.MoviesClient;
import com.eron.challenge.model.api.DirectorsResponse;
import com.eron.challenge.model.external.Movie;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class DirectorsService {

    private final MoviesClient moviesClient;
    private final MeterRegistry meterRegistry;

    public DirectorsService(MoviesClient moviesClient, MeterRegistry meterRegistry) {
        this.moviesClient = moviesClient;
        this.meterRegistry = meterRegistry;
    }

    public Mono<DirectorsResponse> getDirectorsAboveThreshold(int threshold) {
        long start = System.nanoTime();
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
                .map(list -> {
                    meterRegistry.timer("directors.aggregate.timer").record(System.nanoTime() - start,
                            TimeUnit.NANOSECONDS);
                    meterRegistry.summary("directors.aggregate.size").record(list.size());
                    return new DirectorsResponse(list);
                });
    }

}
