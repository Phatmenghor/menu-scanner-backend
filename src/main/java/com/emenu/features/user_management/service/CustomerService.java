package com.emenu.features.user_management.service;

import com.emenu.features.user_management.dto.filter.CustomerFilterRequest;
import com.emenu.features.user_management.dto.request.CreateCustomerRequest;
import com.emenu.features.user_management.dto.response.CustomerResponse;
import com.emenu.features.user_management.dto.update.UpdateCustomerRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface CustomerService {
    CustomerResponse createCustomer(CreateCustomerRequest request);
    CustomerResponse getCustomer(UUID id);
    CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request);
    void addLoyaltyPoints(UUID id, Integer points);
    CustomerResponse getCurrentCustomer();
    PaginationResponse<CustomerResponse> listCustomers(CustomerFilterRequest filter);
}
