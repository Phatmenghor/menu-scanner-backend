package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.PlatformMessageRequest;
import com.emenu.features.auth.dto.request.PlatformUserCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.CustomerResponse;
import com.emenu.features.auth.dto.response.PlatformStatsResponse;
import com.emenu.features.auth.dto.response.PlatformUserResponse;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.features.auth.dto.update.CustomerUpdateRequest;
import com.emenu.features.auth.dto.update.PlatformUserUpdateRequest;
import com.emenu.features.messaging.dto.filter.MessageFilterRequest;
import com.emenu.features.messaging.dto.response.MessageResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface PlatformOwnerService {
    
    // Platform User Management
    PlatformUserResponse createPlatformUser(PlatformUserCreateRequest request);
    PaginationResponse<PlatformUserResponse> getAllPlatformUsers(UserFilterRequest filter);
    PlatformUserResponse getPlatformUserById(UUID id);
    PlatformUserResponse updatePlatformUser(UUID id, PlatformUserUpdateRequest request);
    void deletePlatformUser(UUID id);
    
    // Business Management
    PaginationResponse<BusinessResponse> getAllBusinesses(BusinessFilterRequest filter);
    BusinessResponse getBusinessById(UUID id);
    BusinessResponse updateBusiness(UUID id, BusinessUpdateRequest request);
    void deleteBusiness(UUID id);
    void suspendBusiness(UUID id);
    void activateBusiness(UUID id);
    
    // Customer Management
    PaginationResponse<CustomerResponse> getAllCustomers(UserFilterRequest filter);
    CustomerResponse getCustomerById(UUID id);
    CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request);
    void deleteCustomer(UUID id);
    
    // Platform Messaging
    void sendPlatformMessage(PlatformMessageRequest request);
    PaginationResponse<MessageResponse> getAllMessages(MessageFilterRequest filter);
    MessageResponse getMessageById(UUID id);
    void deleteMessage(UUID id);
    
    // Platform Statistics
    PlatformStatsResponse getPlatformStats();
    
    // System Management
    void lockUser(UUID id);
    void unlockUser(UUID id);
    void resetUserPassword(UUID id);
}