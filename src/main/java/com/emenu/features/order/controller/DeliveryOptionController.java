package com.emenu.features.order.controller;

import com.emenu.features.order.dto.request.DeliveryOptionCreateRequest;
import com.emenu.features.order.dto.response.DeliveryOptionResponse;
import com.emenu.features.order.dto.update.DeliveryOptionUpdateRequest;
import com.emenu.features.order.service.DeliveryOptionService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery-options")
@RequiredArgsConstructor
@Slf4j
public class DeliveryOptionController {

    private final DeliveryOptionService deliveryOptionService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryOptionResponse>> createDeliveryOption(@Valid @RequestBody DeliveryOptionCreateRequest request) {
        DeliveryOptionResponse deliveryOption = deliveryOptionService.createDeliveryOption(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Delivery option created successfully", deliveryOption));
    }

    @GetMapping("/my-business")
    public ResponseEntity<ApiResponse<List<DeliveryOptionResponse>>> getMyBusinessDeliveryOptions() {
        List<DeliveryOptionResponse> deliveryOptions = deliveryOptionService.getMyBusinessDeliveryOptions();
        return ResponseEntity.ok(ApiResponse.success("Delivery options retrieved successfully", deliveryOptions));
    }

    @GetMapping("/business/{businessId}/active")
    public ResponseEntity<ApiResponse<List<DeliveryOptionResponse>>> getActiveDeliveryOptions(@PathVariable UUID businessId) {
        List<DeliveryOptionResponse> deliveryOptions = deliveryOptionService.getActiveDeliveryOptions(businessId);
        return ResponseEntity.ok(ApiResponse.success("Active delivery options retrieved successfully", deliveryOptions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryOptionResponse>> getDeliveryOptionById(@PathVariable UUID id) {
        DeliveryOptionResponse deliveryOption = deliveryOptionService.getDeliveryOptionById(id);
        return ResponseEntity.ok(ApiResponse.success("Delivery option retrieved successfully", deliveryOption));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryOptionResponse>> updateDeliveryOption(
            @PathVariable UUID id, @Valid @RequestBody DeliveryOptionUpdateRequest request) {
        DeliveryOptionResponse deliveryOption = deliveryOptionService.updateDeliveryOption(id, request);
        return ResponseEntity.ok(ApiResponse.success("Delivery option updated successfully", deliveryOption));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryOptionResponse>> deleteDeliveryOption(@PathVariable UUID id) {
        DeliveryOptionResponse deliveryOption = deliveryOptionService.deleteDeliveryOption(id);
        return ResponseEntity.ok(ApiResponse.success("Delivery option deleted successfully", deliveryOption));
    }
}