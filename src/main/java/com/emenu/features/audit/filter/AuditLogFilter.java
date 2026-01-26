package com.emenu.features.audit.filter;

import com.emenu.features.audit.service.AuditLogService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Filter to log all HTTP requests and responses for audit purposes.
 * This filter runs BEFORE authentication, so it logs both authenticated and anonymous requests.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
@Slf4j
public class AuditLogFilter extends OncePerRequestFilter {

    private final AuditLogService auditLogService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        // Skip logging for static resources and health checks
        String uri = request.getRequestURI();
        if (shouldSkipLogging(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap request and response to cache content for logging
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        Exception exception = null;

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long responseTimeMs = System.currentTimeMillis() - startTime;
            int statusCode = wrappedResponse.getStatus();

            // Extract error message if present
            String errorMessage = null;
            if (exception != null) {
                errorMessage = exception.getMessage();
            } else if (statusCode >= 400) {
                errorMessage = "HTTP " + statusCode + " error";
            }

            // Extract request and response bodies for POST/PUT/PATCH requests
            String requestBody = null;
            String responseBody = null;

            if (shouldLogBodies(request.getMethod())) {
                requestBody = getContentAsString(wrappedRequest.getContentAsByteArray());
            }

            // Log the request asynchronously
            try {
                auditLogService.logAccessWithBodies(
                    wrappedRequest,
                    statusCode,
                    responseTimeMs,
                    errorMessage,
                    requestBody
                );
            } catch (Exception e) {
                log.error("Failed to log audit entry: {}", e.getMessage());
            }

            // Copy response body to actual response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private boolean shouldSkipLogging(String uri) {
        // Skip logging for these paths to reduce noise
        return uri.startsWith("/api/images/") ||
               uri.startsWith("/swagger-ui/") ||
               uri.startsWith("/v3/api-docs") ||
               uri.startsWith("/actuator/") ||
               uri.equals("/health") ||
               uri.equals("/favicon.ico") ||
               uri.endsWith(".css") ||
               uri.endsWith(".js") ||
               uri.endsWith(".png") ||
               uri.endsWith(".jpg") ||
               uri.endsWith(".ico");
    }

    private boolean shouldLogBodies(String httpMethod) {
        // Only log request/response bodies for data modification methods
        return "POST".equalsIgnoreCase(httpMethod) ||
               "PUT".equalsIgnoreCase(httpMethod) ||
               "PATCH".equalsIgnoreCase(httpMethod) ||
               "DELETE".equalsIgnoreCase(httpMethod);
    }

    private String getContentAsString(byte[] content) {
        if (content == null || content.length == 0) {
            return null;
        }
        return new String(content, StandardCharsets.UTF_8);
    }
}
