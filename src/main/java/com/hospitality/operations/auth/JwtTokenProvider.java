package com.hospitality.operations.auth;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

    // Base64-encoded 256-bit key — in production, load from application.yml or vault
    private static final String SECRET_BASE64 = "Y29tLmhvc3BpdGFsaXR5Lm9wZXJhdGlvbnMuand0LnNlY3JldC5rZXkuMjAyNg==";
    private static final long EXPIRATION_MS = 86_400_000; // 24 hours

    private final SecretKey signingKey;

    public JwtTokenProvider() {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_BASE64));
    }

    public String generateToken(String username, String role, String tenantSchema) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("tenant", tenantSchema)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public String getTenant(String token) {
        return parseClaims(token).get("tenant", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
