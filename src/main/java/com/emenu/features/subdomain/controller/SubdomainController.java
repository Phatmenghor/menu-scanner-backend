package com.emenu.features.subdomain.controller;

import com.emenu.features.subdomain.dto.filter.SubdomainFilterRequest;
import com.emenu.features.subdomain.dto.response.SubdomainCheckResponse;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
import com.emenu.features.subdomain.dto.update.SubdomainUpdateRequest;
import com.emenu.features.subdomain.service.SubdomainService;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
     * Get subdomain by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubdomainResponse>> getSubdomainById(@PathVariable UUID id) {
        log.info("Getting subdomain by ID: {}", id);
        SubdomainResponse subdomain = subdomainService.getSubdomainById(id);
        return ResponseEntity.ok(ApiResponse.success("Subdomain retrieved successfully", subdomain));
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
     * Enable SSL
     */
    @PostMapping("/{id}/enable-ssl")
    public ResponseEntity<ApiResponse<SubdomainResponse>> enableSSL(@PathVariable UUID id) {
        log.info("Enabling SSL for subdomain: {}", id);
        SubdomainResponse subdomain = subdomainService.enableSSL(id);
        return ResponseEntity.ok(ApiResponse.success("SSL enabled successfully", subdomain));
    }
}