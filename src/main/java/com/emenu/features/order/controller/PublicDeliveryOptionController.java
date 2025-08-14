package com.emenu.features.order.controller;

import com.emenu.features.order.dto.filter.DeliveryOptionFilterRequest;
import com.emenu.features.order.dto.response.DeliveryOptionResponse;
import com.emenu.features.order.service.DeliveryOptionService;
import com.emenu.security.SecurityUtils;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery-options")
@RequiredArgsConstructor
@Slf4j
public class PublicDeliveryOptionController {

    private final DeliveryOptionService deliveryOptionService;
    private final SecurityUtils securityUtils;

    /**
     * Get my business delivery options with filtering
     */
    @PostMapping("/my-business/all")
    public ResponseEntity<ApiResponse<List<DeliveryOptionResponse>>> getMyBusinessDeliveryOptions(
            @Valid @RequestBody DeliveryOptionFilterRequest filter) {
        log.info("Getting delivery options for current user's business");

        UUID businessId = securityUtils.getCurrentUser().getBusinessId();
        filter.setBusinessId(businessId);

        List<DeliveryOptionResponse> deliveryOptions = deliveryOptionService.getAllItemDeliveryOptions(filter);

        return ResponseEntity.ok(ApiResponse.success("Business all delivery options retrieved successfully", deliveryOptions));
    }
}
