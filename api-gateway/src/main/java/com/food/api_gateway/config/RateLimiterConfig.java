package com.food.api_gateway.config;

import com.food.api_gateway.util.GatewayJwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterConfig.class);

    @Autowired
    GatewayJwtUtils jwtUtils;

    // Existing IP-based resolver — kept for public/unauthenticated routes
    @Primary
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = exchange.getRequest()
                    .getRemoteAddress()
                    .getAddress()
                    .getHostAddress();

            log.debug("Rate limiting key (IP): {}", clientIp);
            return Mono.just(clientIp);
        };
    }

    // New JWT-based resolver — for authenticated routes
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                log.debug("JWT token found: {}...", token.substring(0, Math.min(10, token.length())));

                String username = jwtUtils.extractUsername(token);

                if (StringUtils.hasText(username)) {
                    log.info("Rate limiting key: USER - {}", username);
                    return Mono.just(username);
                }
            }
            // Fallback to IP for requests with no valid token (shouldn't normally
            // reach an authenticated route, but keeps rate limiting safe either way)
            String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            log.info("Rate limiting key: ANONYMOUS:{} (IP: {})", clientIp, clientIp);
            return Mono.just("anonymous:" + clientIp);
        };
    }
}
