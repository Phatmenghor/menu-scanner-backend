package com.emenu.exception;

import com.emenu.shared.constants.ErrorCodes;
import com.emenu.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex, HttpServletRequest request) {
        log.error("Custom exception occurred: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", ex.getErrorCode());
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        
        ApiResponse<Object> response = new ApiResponse<>("error", ex.getMessage(), errorDetails);
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed for request to {}: {}", request.getRequestURI(), errors);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", ErrorCodes.VALIDATION_ERROR);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("validationErrors", errors);

        ApiResponse<Object> response = new ApiResponse<>("error", "Validation failed", errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Authentication failed: Bad credentials - {}", ex.getMessage());
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", ErrorCodes.INVALID_CREDENTIALS);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        
        ApiResponse<Object> response = new ApiResponse<>("error", "Invalid credentials", errorDetails);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handleDisabledException(DisabledException ex, HttpServletRequest request) {
        log.warn("Authentication failed: Account disabled - {}", ex.getMessage());
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", ErrorCodes.ACCOUNT_DISABLED);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        
        ApiResponse<Object> response = new ApiResponse<>("error", "Account is disabled", errorDetails);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleLockedException(LockedException ex, HttpServletRequest request) {
        log.warn("Authentication failed: Account locked - {}", ex.getMessage());
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", ErrorCodes.ACCOUNT_LOCKED);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        
        ApiResponse<Object> response = new ApiResponse<>("error", "Account is locked", errorDetails);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for request to {}: {}", request.getRequestURI(), ex.getMessage());
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", ErrorCodes.INSUFFICIENT_PERMISSIONS);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        
        ApiResponse<Object> response = new ApiResponse<>("error", "Access denied", errorDetails);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected exception in request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", ErrorCodes.INTERNAL_SERVER_ERROR);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        
        ApiResponse<Object> response = new ApiResponse<>("error", "Internal server error", errorDetails);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}