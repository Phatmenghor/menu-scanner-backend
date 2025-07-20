package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.request.BusinessMessageRequest;
import com.emenu.features.auth.dto.request.BusinessStaffCreateRequest;
import com.emenu.features.auth.dto.request.CustomerMessageRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.BusinessStaffResponse;
import com.emenu.features.auth.dto.response.BusinessStatsResponse;
import com.emenu.features.auth.dto.response.CustomerResponse;
import com.emenu.features.auth.dto.update.BusinessStaffUpdateRequest;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.features.messaging.dto.response.MessageResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface BusinessService {
    
    // Business Management
    BusinessResponse createBusiness(BusinessCreateRequest request);
    BusinessResponse getBusinessById(UUID id);
    BusinessResponse updateBusiness(UUID id, BusinessUpdateRequest request);
    void deleteBusiness(UUID id);
    BusinessStatsResponse getBusinessStats(UUID id);
    
    // Staff Management
    BusinessStaffResponse createStaff(UUID businessId, BusinessStaffCreateRequest request);
    PaginationResponse<BusinessStaffResponse> getBusinessStaff(UUID businessId, int pageNo, int pageSize);
    BusinessStaffResponse getStaffById(UUID staffId);
    BusinessStaffResponse updateStaff(UUID staffId, BusinessStaffUpdateRequest request);
    void deleteStaff(UUID staffId);
    void activateStaff(UUID staffId);
    void deactivateStaff(UUID staffId);
    
    // Business Messaging
    void sendBusinessMessage(UUID businessId, BusinessMessageRequest request);
    PaginationResponse<MessageResponse> getBusinessMessages(UUID businessId, int pageNo, int pageSize);
    PaginationResponse<MessageResponse> getUnreadMessages(UUID businessId, int pageNo, int pageSize);
    void markMessageAsRead(UUID messageId);
    
    // Customer Management for Business
    PaginationResponse<CustomerResponse> getBusinessCustomers(UUID businessId, int pageNo, int pageSize);
    void sendMessageToCustomer(UUID businessId, UUID customerId, CustomerMessageRequest request);
}