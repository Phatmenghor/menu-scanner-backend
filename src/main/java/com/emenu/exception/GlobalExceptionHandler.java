package com.emenu.exception;

import com.emenu.exception.custom.CustomException;
import com.emenu.exception.custom.UserNotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.constants.ErrorCodes;
import com.emenu.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.AccountLockedException;
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

        Map<String, Object> errorDetails = createErrorDetails(ex.getErrorCode(), request);
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

        Map<String, Object> errorDetails = createErrorDetails(ErrorCodes.VALIDATION_ERROR, request);
        errorDetails.put("validationErrors", errors);

        ApiResponse<Object> response = new ApiResponse<>("error", "Validation failed", errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Authentication failed: Bad credentials - {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorDetails(ErrorCodes.INVALID_CREDENTIALS, request);
        ApiResponse<Object> response = new ApiResponse<>("error", "Invalid email or password", errorDetails);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Enhanced Account Status Exception Handlers
    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountInactiveException(AccountInactiveException ex, HttpServletRequest request) {
        log.warn("Login blocked - Account inactive: {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorDetails(ErrorCodes.ACCOUNT_DISABLED, request);
        errorDetails.put("accountStatus", "INACTIVE");
        errorDetails.put("supportContact", "support@emenu-platform.com");

        ApiResponse<Object> response = new ApiResponse<>("error", ex.getMessage(), errorDetails);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountLockedException(AccountLockedException ex, HttpServletRequest request) {
        log.warn("Login blocked - Account locked: {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorDetails(ErrorCodes.ACCOUNT_LOCKED, request);
        errorDetails.put("accountStatus", "LOCKED");
        errorDetails.put("supportContact", "support@emenu-platform.com");
        errorDetails.put("securityNote", "Your account has been locked for security reasons");

        ApiResponse<Object> response = new ApiResponse<>("error", ex.getMessage(), errorDetails);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(AccountSuspendedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountSuspendedException(AccountSuspendedException ex, HttpServletRequest request) {
        log.warn("Login blocked - Account suspended: {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorDetails(ErrorCodes.ACCOUNT_DISABLED, request);
        errorDetails.put("accountStatus", "SUSPENDED");
        errorDetails.put("supportContact", "support@emenu-platform.com");
        errorDetails.put("appealProcess", "Contact support to appeal this suspension");

        ApiResponse<Object> response = new ApiResponse<>("error", ex.getMessage(), errorDetails);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // Spring Security Account Status Handlers
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Object>> handleDisabledException(DisabledException ex, HttpServletRequest request) {
        log.warn("Login blocked - Account disabled: {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorDetails(ErrorCodes.ACCOUNT_DISABLED, request);
        errorDetails.put("accountStatus", "DISABLED");

        ApiResponse<Object> response = new ApiResponse<>("error", "Your account has been disabled. Please contact support.", errorDetails);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Object>> handleLockedException(LockedException ex, HttpServletRequest request) {
        log.warn("Login blocked - Account locked by Spring Security: {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorDetails(ErrorCodes.ACCOUNT_LOCKED, request);
        ApiResponse<Object> response = new ApiResponse<>("error", "Your account has been locked. Please contact support.", errorDetails);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        log.warn("User not found: {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorDetails(ErrorCodes.USER_NOT_FOUND, request);
        ApiResponse<Object> response = new ApiResponse<>("error", ex.getMessage(), errorDetails);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(ValidationException ex, HttpServletRequest request) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, Object> errorDetails = createErrorDetails(ErrorCodes.VALIDATION_ERROR, request);
        ApiResponse<Object> response = new ApiResponse<>("error", ex.getMessage(), errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for request to {}: {}", request.getRequestURI(), ex.getMessage());

        Map<String, Object> errorDetails = createErrorDetails(ErrorCodes.INSUFFICIENT_PERMISSIONS, request);
        ApiResponse<Object> response = new ApiResponse<>("error", "Access denied. You don't have permission to perform this action.", errorDetails);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation in request to {}: {}", request.getRequestURI(), ex.getMessage());

        String message = "Data constraint violation";
        String errorCode = ErrorCodes.VALIDATION_ERROR;

        if (ex.getMessage().contains("email")) {
            message = "Email already exists";
            errorCode = ErrorCodes.EMAIL_ALREADY_EXISTS;
        } else if (ex.getMessage().contains("phone")) {
            message = "Phone number already exists";
            errorCode = ErrorCodes.PHONE_ALREADY_EXISTS;
        }

        Map<String, Object> errorDetails = createErrorDetails(errorCode, request);
        ApiResponse<Object> response = new ApiResponse<>("error", message, errorDetails);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("Unexpected exception in request to {} [TraceId: {}]: {}", request.getRequestURI(), traceId, ex.getMessage(), ex);

        Map<String, Object> errorDetails = createErrorDetails(ErrorCodes.INTERNAL_SERVER_ERROR, request);
        errorDetails.put("traceId", traceId);

        ApiResponse<Object> response = new ApiResponse<>("error", "An unexpected error occurred. Please try again later.", errorDetails);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private Map<String, Object> createErrorDetails(String errorCode, HttpServletRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("errorCode", errorCode);
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("traceId", UUID.randomUUID().toString());
        return errorDetails;
    }
}
