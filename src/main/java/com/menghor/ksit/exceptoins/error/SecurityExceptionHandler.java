package com.menghor.ksit.exceptoins.error;

import com.menghor.ksit.exceptoins.response.ErrorObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

/**
 * Exception handler specifically for security-related exceptions
 * Handles all Spring Security authentication and authorization exceptions
 */
@ControllerAdvice
public class SecurityExceptionHandler {

    /**
     * Handle access denied exceptions (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorObject> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.FORBIDDEN.value());
        errorObject.setMessage("Access denied: You do not have permission to access this resource");
        errorObject.setTimestamp(new Date());
        errorObject.setPath(path);

        return new ResponseEntity<>(errorObject, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle bad credentials (invalid username/password)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorObject> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        errorObject.setMessage("Invalid username or password");
        errorObject.setTimestamp(new Date());
        errorObject.setPath(path);

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle disabled account (Status.INACTIVE)
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorObject> handleDisabledException(
            DisabledException ex, WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        errorObject.setMessage("Account is inactive. Please contact administrator");
        errorObject.setTimestamp(new Date());
        errorObject.setPath(path);

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle locked account (Status.DELETED)
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorObject> handleLockedException(
            LockedException ex, WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        errorObject.setMessage("Account has been deleted. Please contact administrator");
        errorObject.setTimestamp(new Date());
        errorObject.setPath(path);

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle account expired
     */
    @ExceptionHandler(AccountExpiredException.class)
    public ResponseEntity<ErrorObject> handleAccountExpiredException(
            AccountExpiredException ex, WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        errorObject.setMessage("Account has expired. Please contact administrator");
        errorObject.setTimestamp(new Date());
        errorObject.setPath(path);

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle credentials expired
     */
    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<ErrorObject> handleCredentialsExpiredException(
            CredentialsExpiredException ex, WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        errorObject.setMessage("Password has expired. Please change your password");
        errorObject.setTimestamp(new Date());
        errorObject.setPath(path);

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle username not found
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorObject> handleUsernameNotFoundException(
            UsernameNotFoundException ex, WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        errorObject.setMessage("Invalid username or password");
        errorObject.setTimestamp(new Date());
        errorObject.setPath(path);

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle insufficient authentication
     */
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ErrorObject> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException ex, WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        errorObject.setMessage("Authentication required. Please login to access this resource");
        errorObject.setTimestamp(new Date());
        errorObject.setPath(path);

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle generic authentication exceptions (fallback)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorObject> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();

        ErrorObject errorObject = new ErrorObject();
        errorObject.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        errorObject.setMessage("Authentication failed: " + ex.getMessage());
        errorObject.setTimestamp(new Date());
        errorObject.setPath(path);

        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }
}