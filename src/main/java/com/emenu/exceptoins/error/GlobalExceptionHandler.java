package com.emenu.exceptoins.error;

import com.emenu.exceptoins.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
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
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                        "error",
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        log.warn("Bad request: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        "error",
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(DuplicateNameException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateNameException(DuplicateNameException ex, HttpServletRequest request) {
        log.warn("Duplicate resource: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(
                        "error",
                        ex.getMessage(),
                        null
                ));
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

        // Create a user-friendly message
        String friendlyMessage = createValidationErrorMessage(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        "error",
                        friendlyMessage,
                        errors
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Invalid argument in request to {}: {}", request.getRequestURI(), ex.getMessage());

        // Provide more specific error message based on the exception
        String message = ex.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = "Invalid input provided. Please check your request data and try again.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        "error",
                        message,
                        null
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request to {}: {}", request.getRequestURI(), ex.getMessage());

        String message = determineJsonErrorMessage(ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        "error",
                        message,
                        null
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch for parameter '{}' in request to {}: expected {}",
                ex.getName(), request.getRequestURI(), Objects.requireNonNull(ex.getRequiredType()).getSimpleName());

        String message = String.format("Invalid value for '%s'. Expected %s but received '%s'. Please provide a valid %s value.",
                ex.getName(),
                getHumanReadableType(ex.getRequiredType().getSimpleName()),
                ex.getValue(),
                getHumanReadableType(ex.getRequiredType().getSimpleName()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        "error",
                        message,
                        null
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing required parameter '{}' in request to {}", ex.getParameterName(), request.getRequestURI());

        String message = String.format("Required parameter '%s' is missing from the request. Please include this parameter and try again.",
                ex.getParameterName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                        "error",
                        message,
                        null
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not allowed: {} for path {}", ex.getMethod(), request.getRequestURI());

        String message = String.format("HTTP method '%s' is not supported for this endpoint. Supported methods are: %s",
                ex.getMethod(), String.join(", ", Objects.requireNonNull(ex.getSupportedMethods())));

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ApiResponse<>(
                        "error",
                        message,
                        null
                ));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());

        String message = String.format("The requested endpoint '%s %s' was not found. Please check the URL and method, then try again.",
                ex.getHttpMethod(), ex.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                        "error",
                        message,
                        null
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for request to {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                        "error",
                        "Access denied. You don't have the required permissions to perform this action. Please contact your administrator if you believe this is an error.",
                        null
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation in request to {}: {}", request.getRequestURI(), ex.getMessage());

        String message = determineDataIntegrityErrorMessage(ex);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(
                        "error",
                        message,
                        null
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("Runtime exception in request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        String message = "An unexpected error occurred while processing your request. Please try again later.";

        // Provide more specific message for known runtime exceptions
        if (ex.getMessage() != null) {
            String exMessage = ex.getMessage().toLowerCase();
            if (exMessage.contains("timeout")) {
                message = "The request timed out. Please try again or contact support if the problem persists.";
            } else if (exMessage.contains("connection")) {
                message = "A connection error occurred. Please try again later.";
            } else if (exMessage.contains("not found")) {
                message = "The requested resource could not be found.";
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                        "error",
                        message,
                        null
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected exception in request to {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                        "error",
                        "An internal server error occurred. Our team has been notified. Please try again later or contact support if the problem persists.",
                        Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "path", request.getRequestURI(),
                                "requestId", generateRequestId()
                        )
                ));
    }

    // Helper methods for better error messages

    private String createValidationErrorMessage(Map<String, String> errors) {
        if (errors.size() == 1) {
            Map.Entry<String, String> entry = errors.entrySet().iterator().next();
            return String.format("Validation failed for field '%s': %s", entry.getKey(), entry.getValue());
        } else {
            return String.format("Validation failed for %d fields. Please check the provided data and correct the errors.", errors.size());
        }
    }

    private String determineJsonErrorMessage(HttpMessageNotReadableException ex) {
        String exMessage = ex.getMessage();

        if (exMessage != null) {
            if (exMessage.contains("JSON parse error")) {
                return "Invalid JSON format. Please check your request body for syntax errors such as missing quotes, commas, or brackets.";
            } else if (exMessage.contains("Cannot deserialize")) {
                return "Invalid data format in JSON. Please verify that all field values match the expected data types.";
            } else if (exMessage.contains("Unexpected character")) {
                return "JSON contains unexpected characters. Please ensure proper JSON formatting.";
            } else if (exMessage.contains("Required request body is missing")) {
                return "Request body is required but was not provided. Please include a valid JSON body in your request.";
            }
        }

        return "Invalid JSON request body. Please check your request format and ensure all data is properly formatted.";
    }

    private String determineDataIntegrityErrorMessage(DataIntegrityViolationException ex) {
        String exMessage = ex.getMessage();

        if (exMessage != null) {
            if (exMessage.contains("unique") || exMessage.contains("duplicate")) {
                return "This record already exists. Please use different values for unique fields such as email, username, or ID numbers.";
            } else if (exMessage.contains("foreign key") || exMessage.contains("violates foreign key constraint")) {
                return "Referenced record not found. Please ensure all related records exist before creating this record.";
            } else if (exMessage.contains("not null") || exMessage.contains("null value")) {
                return "Required field is missing. Please provide all mandatory information and try again.";
            } else if (exMessage.contains("check constraint")) {
                return "Data validation failed. Please ensure all field values meet the required constraints.";
            }
        }

        return "Data integrity constraint violation. This operation conflicts with existing data rules. Please review your data and try again.";
    }

    private String getHumanReadableType(String javaType) {
        return switch (javaType.toLowerCase()) {
            case "long", "integer", "int" -> "number";
            case "boolean" -> "true/false value";
            case "localdate" -> "date (YYYY-MM-DD format)";
            case "localdatetime" -> "date and time";
            case "string" -> "text";
            case "double", "float" -> "decimal number";
            default -> javaType.toLowerCase();
        };
    }

    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
}