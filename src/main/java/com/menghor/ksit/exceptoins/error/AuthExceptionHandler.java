package com.menghor.ksit.exceptoins.error;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AuthExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: Bad credentials - {}", ex.getMessage());

        // The message is already specific from our service layer
        String message = ex.getMessage();

        // Provide default message if none is set
        if (message == null || message.trim().isEmpty()) {
            message = "Invalid username or password. Please check your credentials and try again.";
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        message,
                        null
                ));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.warn("Authentication failed: User not found - {}", ex.getMessage());

        // Don't reveal that the username doesn't exist for security reasons
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Invalid username or password. Please check your credentials and try again.",
                        null
                ));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountDisabled(DisabledException ex) {
        log.warn("Authentication failed: Account disabled - {}", ex.getMessage());

        // Use the specific message from our service
        String message = ex.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = "Your account has been temporarily deactivated. Please contact the administrator to reactivate your account.";
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        message,
                        null
                ));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountLocked(LockedException ex) {
        log.warn("Authentication failed: Account locked - {}", ex.getMessage());

        // Use the specific message from our service
        String message = ex.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = "Your account has been permanently deleted and cannot be recovered. Please contact the administrator if you believe this is an error.";
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        message,
                        null
                ));
    }

    @ExceptionHandler(AccountExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountExpired(AccountExpiredException ex) {
        log.warn("Authentication failed: Account expired - {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Your account has expired. Please contact the administrator to renew your account access.",
                        null
                ));
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleCredentialsExpired(CredentialsExpiredException ex) {
        log.warn("Authentication failed: Credentials expired - {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Your password has expired and must be changed. Please contact the administrator for assistance with password reset.",
                        null
                ));
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException ex) {
        log.error("Internal authentication service error: {}", ex.getMessage(), ex);

        // Check if it's caused by our custom exceptions
        Throwable cause = ex.getCause();
        if (cause instanceof DisabledException) {
            return handleAccountDisabled((DisabledException) cause);
        } else if (cause instanceof LockedException) {
            return handleAccountLocked((LockedException) cause);
        } else if (cause instanceof BadCredentialsException) {
            return handleBadCredentials((BadCredentialsException) cause);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                        "error",
                        "An internal authentication error occurred. Please try again later or contact support if the problem persists.",
                        null
                ));
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationServiceException(AuthenticationServiceException ex) {
        log.error("Authentication service error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiResponse<>(
                        "error",
                        "Authentication service is temporarily unavailable. Please try again in a few moments.",
                        null
                ));
    }

    @ExceptionHandler(ProviderNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleProviderNotFound(ProviderNotFoundException ex) {
        log.error("Authentication provider not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                        "error",
                        "Authentication system configuration error. Please contact the administrator.",
                        null
                ));
    }

    @ExceptionHandler(PreAuthenticatedCredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handlePreAuthenticatedCredentialsNotFound(PreAuthenticatedCredentialsNotFoundException ex) {
        log.warn("Pre-authenticated credentials not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        "Authentication credentials are missing or invalid. Please login again.",
                        null
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("General authentication error: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());

        // Fallback for any other authentication exceptions
        String message = ex.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = "Authentication failed. Please check your credentials and try again.";
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(
                        "error",
                        message,
                        null
                ));
    }
}