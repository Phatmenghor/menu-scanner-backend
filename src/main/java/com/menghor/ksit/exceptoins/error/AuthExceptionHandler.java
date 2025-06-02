package com.menghor.ksit.exceptoins.error;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AuthExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Invalid email or password. Please check your credentials and try again.",
                        null
                ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.warn("Authentication failed: User not found - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Invalid email or password. Please check your credentials and try again.",
                        null
                ));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountDisabled(DisabledException ex) {
        log.warn("Authentication failed: Account disabled - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Your account has been disabled. Please contact administrator for assistance.",
                        null
                ));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountLocked(LockedException ex) {
        log.warn("Authentication failed: Account locked - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Your account has been locked. Please contact administrator for assistance.",
                        null
                ));
    }

    @ExceptionHandler(AccountExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountExpired(AccountExpiredException ex) {
        log.warn("Authentication failed: Account expired - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Your account has expired. Please contact administrator for assistance.",
                        null
                ));
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleCredentialsExpired(CredentialsExpiredException ex) {
        log.warn("Authentication failed: Credentials expired - {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Your password has expired. Please reset your password.",
                        null
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Authentication failed. Please check your credentials and try again.",
                        null
                ));
    }
}