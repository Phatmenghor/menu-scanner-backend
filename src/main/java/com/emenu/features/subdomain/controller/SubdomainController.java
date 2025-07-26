package com.emenu.features.subdomain.controller;

import com.emenu.features.subdomain.dto.filter.SubdomainFilterRequest;
import com.emenu.features.subdomain.dto.request.SubdomainCreateRequest;
import com.emenu.features.subdomain.dto.response.SubdomainCheckResponse;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
import com.emenu.features.subdomain.dto.update.SubdomainUpdateRequest;
import com.emenu.features.subdomain.service.SubdomainService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subdomains")
@RequiredArgsConstructor
@Slf4j
public class SubdomainController {

    private final SubdomainService subdomainService;

    /**
     * 🎯 MAIN ENDPOINT FOR FRONTEND - Check subdomain access
     * This is the primary endpoint your frontend will use to check if a subdomain is accessible
     */
    @GetMapping("/check/{subdomain}")
    public ResponseEntity<ApiResponse<SubdomainCheckResponse>> checkSubdomainAccess(@PathVariable String subdomain) {
        log.info("Checking subdomain access for: {}", subdomain);
        SubdomainCheckResponse checkResponse = subdomainService.checkSubdomainAccess(subdomain);
        
        String message = checkResponse.getCanAccess() ? 
            "Subdomain is accessible" : 
            "Subdomain access denied: " + checkResponse.getMessage();
            
        return ResponseEntity.ok(ApiResponse.success(message, checkResponse));
    }

    /**
     * Create new subdomain
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SubdomainResponse>> createSubdomain(@Valid @RequestBody SubdomainCreateRequest request) {
        log.info("Creating subdomain: {} for business: {}", request.getSubdomain(), request.getBusinessId());
        SubdomainResponse subdomain = subdomainService.createSubdomain(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subdomain created successfully", subdomain));
    }

    /**
     * Get all subdomains with filtering and pagination
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<SubdomainResponse>>> getAllSubdomains(
            @Valid @RequestBody SubdomainFilterRequest filter) {
        log.info("Getting all subdomains with filter");
        PaginationResponse<SubdomainResponse> subdomains = subdomainService.getAllSubdomains(filter);
        return ResponseEntity.ok(ApiResponse.success("Subdomains retrieved successfully", subdomains));
    }

    /**
     * Get current user's business subdomains
     */
    @PostMapping("/my-business")
    public ResponseEntity<ApiResponse<PaginationResponse<SubdomainResponse>>> getMyBusinessSubdomains(
            @Valid @RequestBody SubdomainFilterRequest filter) {
        log.info("Getting current user's business subdomains");
        PaginationResponse<SubdomainResponse> subdomains = subdomainService.getCurrentUserBusinessSubdomains(filter);
        return ResponseEntity.ok(ApiResponse.success("Business subdomains retrieved successfully", subdomains));
    }

    /**
     * Get subdomain by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubdomainResponse>> getSubdomainById(@PathVariable UUID id) {
        log.info("Getting subdomain by ID: {}", id);
        SubdomainResponse subdomain = subdomainService.getSubdomainById(id);
        return ResponseEntity.ok(ApiResponse.success("Subdomain retrieved successfully", subdomain));
    }

    /**
     * Get subdomain by name
     */
    @GetMapping("/name/{subdomain}")
    public ResponseEntity<ApiResponse<SubdomainResponse>> getSubdomainByName(@PathVariable String subdomain) {
        log.info("Getting subdomain by name: {}", subdomain);
        SubdomainResponse subdomainResponse = subdomainService.getSubdomainByName(subdomain);
        return ResponseEntity.ok(ApiResponse.success("Subdomain retrieved successfully", subdomainResponse));
    }

    /**
     * Update subdomain
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubdomainResponse>> updateSubdomain(
            @PathVariable UUID id,
            @Valid @RequestBody SubdomainUpdateRequest request) {
        log.info("Updating subdomain: {}", id);
        SubdomainResponse subdomain = subdomainService.updateSubdomain(id, request);
        return ResponseEntity.ok(ApiResponse.success("Subdomain updated successfully", subdomain));
    }

    /**
     * Delete subdomain
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<SubdomainResponse>> deleteSubdomain(@PathVariable UUID id) {
        log.info("Deleting subdomain: {}", id);
        SubdomainResponse subdomain = subdomainService.deleteSubdomain(id);
        return ResponseEntity.ok(ApiResponse.success("Subdomain deleted successfully", subdomain));
    }

    /**
     * Check subdomain availability
     */
    @GetMapping("/availability/{subdomain}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkSubdomainAvailability(@PathVariable String subdomain) {
        log.info("Checking subdomain availability: {}", subdomain);
        boolean isAvailable = subdomainService.isSubdomainAvailable(subdomain);
        
        Map<String, Object> result = Map.of(
            "subdomain", subdomain,
            "available", isAvailable,
            "fullDomain", subdomain + ".menu.com",
            "message", isAvailable ? "Subdomain is available" : "Subdomain is already taken"
        );
        
        String message = isAvailable ? "Subdomain is available" : "Subdomain is not available";
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    /**
     * Get subdomain by business ID
     */
    @GetMapping("/business/{businessId}")
    public ResponseEntity<ApiResponse<SubdomainResponse>> getSubdomainByBusinessId(@PathVariable UUID businessId) {
        log.info("Getting subdomain for business: {}", businessId);
        SubdomainResponse subdomain = subdomainService.getSubdomainByBusinessId(businessId);
        return ResponseEntity.ok(ApiResponse.success("Business subdomain retrieved successfully", subdomain));
    }

    /**
     * Verify domain
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<SubdomainResponse>> verifyDomain(@PathVariable UUID id) {
        log.info("Verifying domain for subdomain: {}", id);
        SubdomainResponse subdomain = subdomainService.verifyDomain(id);
        return ResponseEntity.ok(ApiResponse.success("Domain verified successfully", subdomain));
    }

    /**
     * Enable SSL
     */
    @PostMapping("/{id}/enable-ssl")
    public ResponseEntity<ApiResponse<SubdomainResponse>> enableSSL(@PathVariable UUID id) {
        log.info("Enabling SSL for subdomain: {}", id);
        SubdomainResponse subdomain = subdomainService.enableSSL(id);
        return ResponseEntity.ok(ApiResponse.success("SSL enabled successfully", subdomain));
    }

    /**
     * Suspend subdomain
     */
    @PostMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<SubdomainResponse>> suspendSubdomain(
            @PathVariable UUID id,
            @RequestBody Map<String, String> requestBody) {
        String reason = requestBody.getOrDefault("reason", "Suspended by administrator");
        log.info("Suspending subdomain: {} - Reason: {}", id, reason);
        SubdomainResponse subdomain = subdomainService.suspendSubdomain(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Subdomain suspended successfully", subdomain));
    }

    /**
     * Activate subdomain
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<SubdomainResponse>> activateSubdomain(@PathVariable UUID id) {
        log.info("Activating subdomain: {}", id);
        SubdomainResponse subdomain = subdomainService.activateSubdomain(id);
        return ResponseEntity.ok(ApiResponse.success("Subdomain activated successfully", subdomain));
    }

    /**
     * Get subdomain statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSubdomainStatistics() {
        log.info("Getting subdomain statistics");
        
        Map<String, Object> stats = Map.of(
            "totalSubdomains", subdomainService.getTotalSubdomainsCount(),
            "activeSubdomains", subdomainService.getActiveSubdomainsCount(),
            "timestamp", java.time.LocalDateTime.now()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Subdomain statistics retrieved successfully", stats));
    }

    /**
     * Create subdomain for business (used during business registration - with formatting)
     */
    @PostMapping("/business/{businessId}/auto-create")
    public ResponseEntity<ApiResponse<SubdomainResponse>> createSubdomainForBusiness(
            @PathVariable UUID businessId,
            @RequestBody Map<String, String> requestBody) {
        String preferredSubdomain = requestBody.getOrDefault("preferredSubdomain", "business");
        log.info("Auto-creating subdomain for business: {} with preferred name: {}", businessId, preferredSubdomain);
        
        SubdomainResponse subdomain = subdomainService.createSubdomainForBusiness(businessId, preferredSubdomain);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subdomain created for business successfully", subdomain));
    }

    /**
     * ✅ NEW: Create exact subdomain for business (Platform admins only - minimal formatting)
     */
    @PostMapping("/business/{businessId}/exact")
    public ResponseEntity<ApiResponse<SubdomainResponse>> createExactSubdomainForBusiness(
            @PathVariable UUID businessId,
            @RequestBody Map<String, String> requestBody) {
        String exactSubdomain = requestBody.getOrDefault("exactSubdomain", "business");
        log.info("Creating exact subdomain for business: {} with exact name: {}", businessId, exactSubdomain);
        
        SubdomainResponse subdomain = subdomainService.createExactSubdomainForBusiness(businessId, exactSubdomain);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Exact subdomain created for business successfully", subdomain));
    }
}