package com.menghor.ksit.feature.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JWTGenerator {

    @Value("${jwt.secret.key}")
    private String secretKey; // Injected secret key from properties

    @Value("${jwt.expiration-min}")
    private long jwtExpirationInMinutes;

    /**
     * Public method to get signing key
     * @return Key used for signing and verifying JWT tokens
     */
    public Key getSigningKey() {
        return new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS512.getJcaName());
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();

        long expirationTimeInMs = jwtExpirationInMinutes * 60 * 1000;

        Date expireDate = new Date(currentDate.getTime() + expirationTimeInMs);
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .setSubject(username)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "JWT token has expired");
        } catch (MalformedJwtException e) {
            throw new MalformedJwtException("JWT token is malformed");
        } catch (SignatureException e) {
            throw new SignatureException("JWT signature validation failed");
        } catch (UnsupportedJwtException e) {
            throw new UnsupportedJwtException("JWT token is not supported");
        } catch (IllegalArgumentException e) {
            throw new AuthenticationCredentialsNotFoundException("JWT token is invalid");
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "JWT token has expired");
        } catch (MalformedJwtException e) {
            throw new MalformedJwtException("JWT token is malformed");
        } catch (SignatureException e) {
            throw new SignatureException("JWT signature validation failed");
        } catch (UnsupportedJwtException e) {
            throw new UnsupportedJwtException("JWT token is not supported");
        } catch (IllegalArgumentException e) {
            throw new AuthenticationCredentialsNotFoundException("JWT token is invalid");
        } catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("JWT token validation failed: " + e.getMessage());
        }
    }

    /**
     * Additional method to get token expiration
     * @param token JWT token
     * @return Expiration date of the token
     */
    public Date getTokenExpiration(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "JWT token has expired");
        } catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("Unable to get token expiration: " + e.getMessage());
        }
    }

    /**
     * Check if token is expired without throwing exception
     * @param token JWT token
     * @return true if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getTokenExpiration(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true; // Consider invalid tokens as expired
        }
    }
}