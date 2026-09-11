package com.food.api_gateway.filter;

import com.food.api_gateway.util.GatewayJwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final GatewayJwtUtils jwtUtils;

    public JwtAuthenticationFilter(GatewayJwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    // Routes that do NOT require a valid JWT
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/signin",
            "/api/auth/signup"
    );

    // Public GET endpoints for browsing (no authentication required)
    private static final List<String> PUBLIC_GET_PATHS = List.of(
            "/restaurant/fetchAllRestaurant",
            "/restaurant/fetchById",
            "/restaurant/image",
            "/foodCatalogue",
            "/user/image"
    );

    // Routes that require authentication AND a specific role.
    // Key = path prefix, Value = required role.
    private static final Map<String, String> ADMIN_ONLY_ROUTES = Map.of(
            "/restaurant/addRestaurant", "ROLE_ADMIN",
            "/restaurant/uploadImage/", "ROLE_ADMIN"
    );


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        log.info("Processing request: {}", path);

        // Check if it's a public GET endpoint (browsing)
        boolean isPublicGet = PUBLIC_GET_PATHS.stream()
                .anyMatch(path::startsWith) && HttpMethod.GET.equals(method);

        boolean isAuthEndpoint =PUBLIC_PATHS.stream().anyMatch(path::startsWith);

        if (isPublicGet || isAuthEndpoint) {
            log.info("Public path accessed: {}, no authentication required", path);
            return chain.filter(exchange);
        }

        log.info("Protected path accessed: {}, validating JWT", path);
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or malformed Authorization header for path: {}", path);
            return unauthorized(exchange, HttpStatus.UNAUTHORIZED, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7);
        log.debug("Token extracted successfully for path: {}", path);

        if (!jwtUtils.isTokenValid(token)) {
            log.warn("Invalid or expired token for path: {}", path);
            return unauthorized(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String requiredRole = ADMIN_ONLY_ROUTES.entrySet().stream()
                .filter(entry -> path.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        if (requiredRole != null) {
            List<String> userRoles = jwtUtils.extractRoles(token);
            if (!userRoles.contains(requiredRole)) {
                return unauthorized(exchange, HttpStatus.FORBIDDEN,
                        "You do not have permission to perform this action");
            }
        }

        log.info("JWT validation successful for path: {}", path);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -50;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        String body = String.format(
                "{\"timestamp\":\"%s\",\"message\":\"%s\",\"details\":\"uri=%s\"}",
                java.time.Instant.now(), message, exchange.getRequest().getURI().getPath()
        );
        org.springframework.core.io.buffer.DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
