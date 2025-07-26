package com.emenu.features.subdomain.service;

import com.emenu.features.subdomain.dto.filter.SubdomainFilterRequest;
import com.emenu.features.subdomain.dto.request.SubdomainCreateRequest;
import com.emenu.features.subdomain.dto.response.SubdomainCheckResponse;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
import com.emenu.features.subdomain.dto.update.SubdomainUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface SubdomainService {
    
    // Basic CRUD Operations
    SubdomainResponse createSubdomain(SubdomainCreateRequest request);
    PaginationResponse<SubdomainResponse> getAllSubdomains(SubdomainFilterRequest filter);
    SubdomainResponse getSubdomainById(UUID id);
    SubdomainResponse getSubdomainByName(String subdomain);
    SubdomainResponse updateSubdomain(UUID id, SubdomainUpdateRequest request);
    SubdomainResponse deleteSubdomain(UUID id);
    
    // Business-specific operations
    SubdomainResponse getSubdomainByBusinessId(UUID businessId);
    PaginationResponse<SubdomainResponse> getCurrentUserBusinessSubdomains(SubdomainFilterRequest filter);
    
    // Frontend check operation (main endpoint for frontend)
    SubdomainCheckResponse checkSubdomainAccess(String subdomain);
    
    // Domain management
    boolean isSubdomainAvailable(String subdomain);
    SubdomainResponse verifyDomain(UUID id);
    SubdomainResponse enableSSL(UUID id);
    SubdomainResponse suspendSubdomain(UUID id, String reason);
    SubdomainResponse activateSubdomain(UUID id);
    
    // Statistics and monitoring
    long getTotalSubdomainsCount();
    long getActiveSubdomainsCount();
    
    // Auto-creation for business registration (with formatting)
    SubdomainResponse createSubdomainForBusiness(UUID businessId, String preferredSubdomain);
    
    // ✅ NEW: Exact subdomain creation for platform admins (no formatting)
    SubdomainResponse createExactSubdomainForBusiness(UUID businessId, String exactSubdomain);
}