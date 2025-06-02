package com.menghor.ksit.exceptoins.error;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.security.SignatureException;

@RestControllerAdvice
@Slf4j
public class JWTExceptionHandler {

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleExpiredJwtException(ExpiredJwtException ex) {
        log.warn("JWT token has expired: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Your session has expired. Please login again to continue.",
                        null
                ));
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleMalformedJwtException(MalformedJwtException ex) {
        log.warn("Malformed JWT token: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Invalid authentication token. Please login again.",
                        null
                ));
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiResponse<Object>> handleSignatureException(SignatureException ex) {
        log.warn("Invalid JWT signature: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Invalid authentication token signature. Please login again.",
                        null
                ));
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnsupportedJwtException(UnsupportedJwtException ex) {
        log.warn("Unsupported JWT token: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Unsupported authentication token format. Please login again.",
                        null
                ));
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationCredentialsNotFoundException(
            AuthenticationCredentialsNotFoundException ex) {
        log.warn("Authentication credentials not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Authentication token is missing or invalid. Please login to access this resource.",
                        null
                ));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Object>> handleJwtException(JwtException ex) {
        log.warn("JWT processing error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Authentication token error. Please login again.",
                        null
                ));
    }
}