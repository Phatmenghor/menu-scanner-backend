package com.emenu.features.location.service;

import com.emenu.features.location.dto.filter.LocationFilterRequest;
import com.emenu.features.location.dto.request.LocationCreateRequest;
import com.emenu.features.location.dto.response.LocationResponse;
import com.emenu.features.location.dto.update.LocationUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.List;
import java.util.UUID;

public interface LocationService {
    LocationResponse createAddress(LocationCreateRequest request);
    
    PaginationResponse<LocationResponse> getAllAddresses(LocationFilterRequest filter);
    
    List<LocationResponse> getMyAddressesList();

    LocationResponse getAddressById(UUID id);
    LocationResponse updateAddress(UUID id, LocationUpdateRequest request);
    LocationResponse deleteAddress(UUID id);
    LocationResponse getDefaultAddress();
}