package com.emenu.features.location.service;

import com.emenu.features.location.dto.filter.CustomerAddressFilterRequest;
import com.emenu.features.location.dto.request.CustomerAddressCreateRequest;
import com.emenu.features.location.dto.response.CustomerAddressResponse;
import com.emenu.features.location.dto.update.CustomerAddressUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerAddressService {
    CustomerAddressResponse createAddress(CustomerAddressCreateRequest request);
    
    // Pagination methods
    PaginationResponse<CustomerAddressResponse> getAllAddresses(CustomerAddressFilterRequest filter);
    
    // List methods (backward compatibility)
    List<CustomerAddressResponse> getMyAddressesList();
    List<CustomerAddressResponse> getMyAddresses(); // Deprecated - use getMyAddressesList()
    
    CustomerAddressResponse getAddressById(UUID id);
    CustomerAddressResponse updateAddress(UUID id, CustomerAddressUpdateRequest request);
    CustomerAddressResponse deleteAddress(UUID id);
    CustomerAddressResponse setDefaultAddress(UUID id);
    CustomerAddressResponse getDefaultAddress();
}