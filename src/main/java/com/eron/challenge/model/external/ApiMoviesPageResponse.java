package com.eron.challenge.model.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ApiMoviesPageResponse(
        int page,
        @JsonProperty("per_page") int perPage,
        int total,
        @JsonProperty("total_page")
        int totalPages,
        List<Movie> data
) {}
