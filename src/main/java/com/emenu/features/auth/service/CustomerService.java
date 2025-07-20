package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.CustomerCreateRequest;
import com.emenu.features.auth.dto.request.CustomerMessageRequest;
import com.emenu.features.auth.dto.response.CustomerResponse;
import com.emenu.features.auth.dto.update.CustomerUpdateRequest;
import com.emenu.features.messaging.dto.response.MessageResponse;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface CustomerService {
    
    // Customer Management
    CustomerResponse createCustomer(CustomerCreateRequest request);
    PaginationResponse<CustomerResponse> getCustomers(int pageNo, int pageSize, String search);
    CustomerResponse getCustomerById(UUID id);
    CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request);
    void deleteCustomer(UUID id);
    void activateCustomer(UUID id);
    void deactivateCustomer(UUID id);
    
    // Customer Messaging
    void sendMessageToCustomer(UUID customerId, CustomerMessageRequest request);
    PaginationResponse<MessageResponse> getCustomerMessages(UUID customerId, int pageNo, int pageSize);
    void markMessageAsRead(UUID messageId);
    
    // Customer Self-Service
    CustomerResponse getCurrentCustomerProfile();
    CustomerResponse updateCurrentCustomerProfile(CustomerUpdateRequest request);
    PaginationResponse<MessageResponse> getCurrentCustomerMessages(int pageNo, int pageSize);
    void sendMessageFromCustomer(CustomerMessageRequest request);
}
