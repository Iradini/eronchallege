package com.eron.challenge.controller;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = DirectorsController.class)
@Import(RestExceptionHandler.class)
public class DirectorsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private DirectorsService directorsService;

    @Test
    public void testReturnsDirectorsForValidationThreshold() {
        when(directorsService.getDirectorsAboveThreshold(anyInt()))
                .thenReturn(Mono.just(new DirectorsResponse(List.of("Martin Scorsese", "WoodyAllen"))));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/directors").queryParam("threshold", 4).build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.directors[0]").isEqualTo("Martin Scorsese")
                .jsonPath("$.directors[1]").isEqualTo("Woody Allen");
    }

    @Test
    public void testReturnsBadRequestThreshold() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/directors").queryParam("threshold", -1).build())
                .exchange()
                .expectStatus().isBadRequest();
    }
}
