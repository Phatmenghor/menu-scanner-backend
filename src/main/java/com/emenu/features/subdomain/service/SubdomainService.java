package com.emenu.features.subdomain.service;

import com.emenu.features.subdomain.dto.filter.SubdomainFilterRequest;
import com.emenu.features.subdomain.dto.request.SubdomainGenerateRequest;
import com.emenu.features.subdomain.dto.response.SubdomainCheckResponse;
import com.emenu.features.subdomain.dto.response.SubdomainGenerateResponse;
import com.emenu.features.subdomain.dto.response.SubdomainResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface SubdomainService {
    
    // Basic Operations
    PaginationResponse<SubdomainResponse> getAllSubdomains(SubdomainFilterRequest filter);
    SubdomainResponse getSubdomainById(UUID id);
    SubdomainResponse getSubdomainByBusinessId(UUID businessId);
    
    // Main Frontend Check Operation
    SubdomainCheckResponse checkSubdomainAccess(String subdomain);
    
    // Domain Management
    boolean isSubdomainAvailable(String subdomain);
    
    // Auto-creation for business registration (MAIN METHOD)
    SubdomainResponse createSubdomainForBusiness(UUID businessId, String preferredSubdomain);

    SubdomainGenerateResponse generateSubdomainSuggestions(SubdomainGenerateRequest request);
}