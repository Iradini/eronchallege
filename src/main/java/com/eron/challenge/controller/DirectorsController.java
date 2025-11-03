package com.eron.challenge.controller;

import com.eron.challenge.model.api.DirectorsResponse;
import com.eron.challenge.service.DirectorsService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Validated
@RestController
@Timed(value = "api.directors.request",histogram = true)

public class DirectorsController {

    private final DirectorsService directorsService;
    public DirectorsController(DirectorsService directorsService) { this.directorsService = directorsService; }

    @Operation(summary = "Directors above threshold", description = "Returns directors with movie count strictly greater than threshold")
    @Parameter(name = "threshold", description = "Strictly greater than", required = true, example = "4")
    @GetMapping("/api/directors")
    public Mono<DirectorsResponse> getDirectors(@RequestParam("threshold") @Min(0) int threshold) {
        return directorsService.getDirectorsAboveThreshold(threshold);
    }
}
