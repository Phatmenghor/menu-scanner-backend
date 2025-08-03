package com.emenu.features.order.controller;

import com.emenu.features.order.dto.filter.DeliveryOptionFilterRequest;
import com.emenu.features.order.dto.request.DeliveryOptionCreateRequest;
import com.emenu.features.order.dto.response.DeliveryOptionResponse;
import com.emenu.features.order.dto.update.DeliveryOptionUpdateRequest;
import com.emenu.features.order.service.DeliveryOptionService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
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

    /**
     * Create new delivery option
     */
    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryOptionResponse>> createDeliveryOption(
            @Valid @RequestBody DeliveryOptionCreateRequest request) {
        log.info("Creating delivery option: {}", request.getName());
        DeliveryOptionResponse deliveryOption = deliveryOptionService.createDeliveryOption(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Delivery option created successfully", deliveryOption));
    }

    /**
     * Get all delivery options with filtering
     */
    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<DeliveryOptionResponse>>> getAllDeliveryOptions(
            @Valid @RequestBody DeliveryOptionFilterRequest filter) {
        log.info("Getting all delivery options with filters");
        PaginationResponse<DeliveryOptionResponse> deliveryOptions =
                deliveryOptionService.getAllDeliveryOptions(filter);
        return ResponseEntity.ok(ApiResponse.success("Delivery options retrieved successfully", deliveryOptions));
    }

    /**
     * Get my business delivery options with filtering
     */
    @PostMapping("/my-business/all")
    public ResponseEntity<ApiResponse<PaginationResponse<DeliveryOptionResponse>>> getMyBusinessDeliveryOptions(
            @Valid @RequestBody DeliveryOptionFilterRequest filter) {
        log.info("Getting delivery options for current user's business");

        UUID businessId = securityUtils.getCurrentUser().getBusinessId();
        filter.setBusinessId(businessId);

        PaginationResponse<DeliveryOptionResponse> deliveryOptions =
                deliveryOptionService.getAllDeliveryOptions(filter);

        return ResponseEntity.ok(ApiResponse.success("Business delivery options retrieved successfully", deliveryOptions));
    }

    /**
     * Get delivery option by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryOptionResponse>> getDeliveryOptionById(@PathVariable UUID id) {
        log.info("Getting delivery option by ID: {}", id);
        DeliveryOptionResponse deliveryOption = deliveryOptionService.getDeliveryOptionById(id);
        return ResponseEntity.ok(ApiResponse.success("Delivery option retrieved successfully", deliveryOption));
    }

    /**
     * Update delivery option
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryOptionResponse>> updateDeliveryOption(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryOptionUpdateRequest request) {
        log.info("Updating delivery option: {}", id);
        DeliveryOptionResponse deliveryOption = deliveryOptionService.updateDeliveryOption(id, request);
        return ResponseEntity.ok(ApiResponse.success("Delivery option updated successfully", deliveryOption));
    }

    /**
     * Delete delivery option
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryOptionResponse>> deleteDeliveryOption(@PathVariable UUID id) {
        log.info("Deleting delivery option: {}", id);
        DeliveryOptionResponse deliveryOption = deliveryOptionService.deleteDeliveryOption(id);
        return ResponseEntity.ok(ApiResponse.success("Delivery option deleted successfully", deliveryOption));
    }
}