package com.emenu.features.order.service;

import com.emenu.features.order.dto.request.DeliveryOptionCreateRequest;
import com.emenu.features.order.dto.response.DeliveryOptionResponse;
import com.emenu.features.order.dto.update.DeliveryOptionUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface DeliveryOptionService {
    DeliveryOptionResponse createDeliveryOption(DeliveryOptionCreateRequest request);
    List<DeliveryOptionResponse> getMyBusinessDeliveryOptions();
    List<DeliveryOptionResponse> getActiveDeliveryOptions(UUID businessId);
    DeliveryOptionResponse getDeliveryOptionById(UUID id);
    DeliveryOptionResponse updateDeliveryOption(UUID id, DeliveryOptionUpdateRequest request);
    DeliveryOptionResponse deleteDeliveryOption(UUID id);
}
