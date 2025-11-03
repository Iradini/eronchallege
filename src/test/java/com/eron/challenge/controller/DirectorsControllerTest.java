package com.eron.challenge.controller;

import com.eron.challenge.model.api.DirectorsResponse;
import com.eron.challenge.service.DirectorsService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = DirectorsController.class)
@Import(RestExceptionHandler.class)
public class DirectorsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private DirectorsService directorsService;
    @MockitoBean
    private MeterRegistry meterRegistry;

    @Test
    public void testReturnsDirectorsForValidThreshold() {
        when(directorsService.getDirectorsAboveThreshold(anyInt()))
                .thenReturn(Mono.just(new DirectorsResponse(List.of("Martin Scorsese", "Woody Allen"))));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/directors").queryParam("threshold", 4).build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.directors[0]").isEqualTo("Martin Scorsese")
                .jsonPath("$.directors[1]").isEqualTo("Woody Allen");
    }

    @Test
    public void testReturnGatewayTimeoutOnUpstreamTimeout() {
        when(directorsService.getDirectorsAboveThreshold(4)).thenReturn(Mono.error(new TimeoutException("upstream timeout")));

        webTestClient.get()
                .uri("/api/directors?threshold=4")
                .accept(MediaType.APPLICATION_PROBLEM_JSON)
                .exchange().expectStatus().isEqualTo(HttpStatus.GATEWAY_TIMEOUT)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Upstream timeout");
    }

    @Test
    public void testReturnsBadRequestThreshold() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/directors").queryParam("threshold", -1).build())
                .exchange()
                .expectStatus().isBadRequest();
    }
}
