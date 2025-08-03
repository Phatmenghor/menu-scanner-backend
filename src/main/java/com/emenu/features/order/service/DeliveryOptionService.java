package com.emenu.features.order.service;

import com.emenu.features.order.dto.filter.DeliveryOptionFilterRequest;
import com.emenu.features.order.dto.request.DeliveryOptionCreateRequest;
import com.emenu.features.order.dto.response.DeliveryOptionResponse;
import com.emenu.features.order.dto.update.DeliveryOptionUpdateRequest;
import com.emenu.shared.dto.PaginationResponse;

import java.util.UUID;

public interface DeliveryOptionService {
    
    // CRUD Operations
    DeliveryOptionResponse createDeliveryOption(DeliveryOptionCreateRequest request);
    PaginationResponse<DeliveryOptionResponse> getAllDeliveryOptions(DeliveryOptionFilterRequest filter);
    DeliveryOptionResponse getDeliveryOptionById(UUID id);
    DeliveryOptionResponse updateDeliveryOption(UUID id, DeliveryOptionUpdateRequest request);
    DeliveryOptionResponse deleteDeliveryOption(UUID id);
}