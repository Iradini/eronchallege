package com.eron.challenge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ChallengeApplicationTests {

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        r.add("movies.base-url", () -> "http://localhost");
        r.add("movies.connect-timeout-ms", () -> 200);
        r.add("movies.response-timeout-ms", () -> 200);
        r.add("movies.max-retries", () -> 0);
    }

	@Test
	void contextLoads() {
	}

}
