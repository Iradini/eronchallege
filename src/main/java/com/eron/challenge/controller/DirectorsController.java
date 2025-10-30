package com.eron.challenge.controller;

import com.eron.challenge.service.DirectorsService;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class DirectorsController {

    private final DirectorsService directorsService;

    public DirectorsController(DirectorsService directorsService) { this.directorsService = directorsService; }

    @GetMapping("/api/directors")
    public Mono<DirectorsResponse> getDirectors(@RequestParam("threshold") @Min(0) int threshold) {
        return directorsService.getDirectorsServiceAboveThreshold(threshold);
    }
}
