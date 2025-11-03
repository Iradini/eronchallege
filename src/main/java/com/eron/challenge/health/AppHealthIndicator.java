package com.eron.challenge.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AppHealthIndicator implements ReactiveHealthIndicator {

  @Override
  public Mono<Health> health() {
    // Basic indicator
    return Mono.just(Health.up().withDetail("app", "ready").build());
  }
}

