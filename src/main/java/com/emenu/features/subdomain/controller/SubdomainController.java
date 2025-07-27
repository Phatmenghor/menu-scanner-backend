package com.emenu.features.subdomain.controller;

import com.emenu.features.subdomain.dto.filter.SubdomainFilterRequest;
import com.emenu.features.subdomain.dto.request.SubdomainGenerateRequest;
import com.emenu.features.subdomain.dto.response.SubdomainCheckResponse;
import com.emenu.features.subdomain.dto.response.SubdomainGenerateResponse;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
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
     * 🎯 MAIN ENDPOINT - Check subdomain access for frontend
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
     * Get all subdomains with filtering (Admin only)
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
            "fullUrl", "https://" + subdomain + ".menu.com",
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
     * Generate subdomain by business name
     */
    @PostMapping("/business/generate")
    public ResponseEntity<ApiResponse<SubdomainGenerateResponse>> generateSubdomainByBusinessName(@Valid @RequestBody SubdomainGenerateRequest request) {
        log.info("Getting subdomain for business name: {}", request.getBusinessName());
        SubdomainGenerateResponse generateResponse = subdomainService.generateSubdomainSuggestions(request);
        return ResponseEntity.ok(ApiResponse.success("Subdomain name generate retrieved successfully", generateResponse));
    }
}