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
                        path("/mock-meli-api")
                        .filters(f -> f.hystrix( config -> config.setName("mock-meli-api"))  )
                        .uri("http://localhost:8081/proxy"))
                .route( p -> p.
                        path("/**")
                        .filters(f -> f.hystrix( config -> config.setName("meli-api")))
                        .uri("https://api.mercadolibre.com"))
                .route("fortune_api", p -> p.path("/api-limited")
                        .filters(f -> f.hystrix( config -> config.setName("api-limited"))
                                .requestRateLimiter().rateLimiter(RedisRateLimiter.class,
                                        c -> c.setBurstCapacity(1).setReplenishRate(1))
                                .configure(c -> c.setKeyResolver(exchange -> Mono.just("1"))))
                                .uri("http://localhost:8081/proxy"))
          /*    .route(p -> p
                    .path("/api-limited")
              .filters(f -> f.addRequestHeader("Hello", "World"))
                .uri("http://localhost:8081/proxy"))
                .route(p -> p
                        .host("*.hystrix.com")
                        .filters(f -> f
                                .hystrix(config -> config
                                        .setName("mycmd")))
                        .uri("http://localhost:8081/proxy"))*/
                .build();


    }

}

/*
 cloud:
         gateway:
         routes:

         - id: meliapi
         uri: http://localhost:8081/proxy
         order: 10000
         filters:
         - Hystrix=myCommandName

         predicates:
         - Path=/meli-api

         - id: meliCustomServiceLimit
         uri: http://localhost:8081/proxy
         order: 20000
         filters:
         - name: RequestRateLimiter
         args:
         redis-rate-limiter.replenishRate: 1
         redis-rate-limiter.burstCapacity: 2
         predicates:
         - Path=/controlled-api

         - id: meliCustomService
         uri: https://api.mercadolibre.com/
         order: 30000

         predicates:
         - Path=/**


 */