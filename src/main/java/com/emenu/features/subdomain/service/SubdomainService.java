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
    SubdomainResponse updateSubdomain(UUID id, SubdomainUpdateRequest request);
    SubdomainResponse deleteSubdomain(UUID id);
    
    // Business-specific operations
    SubdomainResponse getSubdomainByBusinessId(UUID businessId);
    PaginationResponse<SubdomainResponse> getCurrentUserBusinessSubdomains(SubdomainFilterRequest filter);
    
    // Frontend check operation (main endpoint for frontend)
    SubdomainCheckResponse checkSubdomainAccess(String subdomain);
    
    // Domain management
    boolean isSubdomainAvailable(String subdomain);
    SubdomainResponse enableSSL(UUID id);
    
    // Auto-creation for business registration (with formatting)
    void createSubdomainForBusiness(UUID businessId, String preferredSubdomain);
    
    // ✅ NEW: Exact subdomain creation for platform admins (no formatting)
    void createExactSubdomainForBusiness(UUID businessId, String exactSubdomain);
}