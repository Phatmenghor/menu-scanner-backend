package com.emenu.features.customer.service;

import com.emenu.features.customer.dto.request.CustomerAddressCreateRequest;
import com.emenu.features.customer.dto.response.CustomerAddressResponse;
import com.emenu.features.customer.dto.update.CustomerAddressUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface CustomerAddressService {
    CustomerAddressResponse createAddress(CustomerAddressCreateRequest request);
    List<CustomerAddressResponse> getMyAddresses();
    CustomerAddressResponse getAddressById(UUID id);
    CustomerAddressResponse updateAddress(UUID id, CustomerAddressUpdateRequest request);
    CustomerAddressResponse deleteAddress(UUID id);
    CustomerAddressResponse setDefaultAddress(UUID id);
    CustomerAddressResponse getDefaultAddress();
}
