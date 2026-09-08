package com.food.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GatewayJwtUtils {

    @Value("${app.jwt.secret}")
    private String secret;

    public String extractUsername(String token) {
        try {
            return parseClaims(token)
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith((SecretKey) getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            Claims claims = parseClaims(token);
            Object rolesClaim = claims.get("roles");

            if (rolesClaim instanceof List<?> rawList) {
                return rawList.stream()
                        .map(item -> {
                            if (item instanceof Map<?, ?> map && map.containsKey("authority")) {
                                return String.valueOf(map.get("authority"));
                            }
                            return String.valueOf(item);
                        })
                        .collect(Collectors.toList());
            }
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }


}
