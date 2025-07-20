package com.emenu.exception;

import com.emenu.security.SecurityUtils;
import com.emenu.shared.constants.ErrorCodes;
import com.emenu.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final SecurityUtils securityUtils;

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex, HttpServletRequest request) {
        log.error("Custom exception occurred: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", ex.getErrorCode());
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("traceId", UUID.randomUUID().toString());

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
        errorDetails.put("traceId", UUID.randomUUID().toString());

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
        errorDetails.put("traceId", UUID.randomUUID().toString());

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
        errorDetails.put("traceId", UUID.randomUUID().toString());

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
        errorDetails.put("traceId", UUID.randomUUID().toString());

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
        errorDetails.put("traceId", UUID.randomUUID().toString());

        ApiResponse<Object> response = new ApiResponse<>("error", "Access denied", errorDetails);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation in request to {}: {}", request.getRequestURI(), ex.getMessage());

        String message = "Data constraint violation";
        if (ex.getMessage().contains("email")) {
            message = "Email already exists";
        } else if (ex.getMessage().contains("phone")) {
            message = "Phone number already exists";
        }

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", ErrorCodes.VALIDATION_ERROR);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("traceId", UUID.randomUUID().toString());

        ApiResponse<Object> response = new ApiResponse<>("error", message, errorDetails);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not supported for request to {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", "METHOD_NOT_SUPPORTED");
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("supportedMethods", ex.getSupportedMethods());
        errorDetails.put("traceId", UUID.randomUUID().toString());

        ApiResponse<Object> response = new ApiResponse<>("error", "HTTP method not supported", errorDetails);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("No handler found for request to {}", request.getRequestURI());

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", "NOT_FOUND");
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("traceId", UUID.randomUUID().toString());

        ApiResponse<Object> response = new ApiResponse<>("error", "Endpoint not found", errorDetails);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Message not readable for request to {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", "MALFORMED_REQUEST");
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("traceId", UUID.randomUUID().toString());

        ApiResponse<Object> response = new ApiResponse<>("error", "Malformed JSON request", errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch for request to {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", "TYPE_MISMATCH");
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("parameter", ex.getName());
        errorDetails.put("expectedType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown");
        errorDetails.put("traceId", UUID.randomUUID().toString());

        ApiResponse<Object> response = new ApiResponse<>("error", "Parameter type mismatch", errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing parameter for request to {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", "MISSING_PARAMETER");
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("missingParameter", ex.getParameterName());
        errorDetails.put("parameterType", ex.getParameterType());
        errorDetails.put("traceId", UUID.randomUUID().toString());

        ApiResponse<Object> response = new ApiResponse<>("error", "Missing required parameter: " + ex.getParameterName(), errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("Unexpected exception in request to {} [TraceId: {}]: {}", request.getRequestURI(), traceId, ex.getMessage(), ex);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", ErrorCodes.INTERNAL_SERVER_ERROR);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("traceId", traceId);

        ApiResponse<Object> response = new ApiResponse<>("error", "Internal server error", errorDetails);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}