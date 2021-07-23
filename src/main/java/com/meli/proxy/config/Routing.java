package com.meli.proxy.config;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;

import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
@EnableHystrix
public class Routing {
    @Bean
    public RouteLocator meliRoutes(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route( p -> p.
                        path("/mock-meli-api/**")
                        .filters(f -> f.hystrix( config -> config.setName("mock-meli-api"))
                                .rewritePath("http://localhost:8081/mock-meli-api/(?<segment>.*)","$\\{segment}"))
                        .uri("http://localhost:8081"))
                .route( p -> p.
                        path("/**")
                        .filters(f -> f.hystrix( config -> config.setName("meli-api"))
                                .rewritePath("https://api.mercadolibre.com/(?<segment>.*)","$\\{segment}"))
                        .uri("https://api.mercadolibre.com"))
                .route("fortune_api", p -> p.path("/api-limited")
                        .filters(f -> f.hystrix( config -> config.setName("api-limited"))
                                .requestRateLimiter().rateLimiter(RedisRateLimiter.class,
                                        c -> c.setBurstCapacity(1).setReplenishRate(1))
                                .configure(c -> c.setKeyResolver(exchange -> Mono.just("1"))))
                                .uri("http://localhost:8081/proxy"))
                .build();


    }

}