package com.emenu.features.order.controller;

import com.emenu.features.order.service.OrderDataGeneratorService;
import com.emenu.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dev/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderDataGeneratorController {

    private final OrderDataGeneratorService dataGeneratorService;

    @PostMapping("/generate-statuses/{businessId}")
    public ResponseEntity<ApiResponse<String>> generateOrderStatuses(@PathVariable UUID businessId) {
        log.info("Generating order statuses for business: {}", businessId);

        dataGeneratorService.generateOrderStatuses(businessId);

        return ResponseEntity.ok(ApiResponse.success("Order statuses generated successfully"));
    }

    @PostMapping("/generate/{businessId}")
    public ResponseEntity<ApiResponse<String>> generateOrders(
            @PathVariable UUID businessId,
            @RequestParam(defaultValue = "80000") int count,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("Generating {} orders for business: {}", count, businessId);

        // Default date range: 1 year before to 1 year after today
        LocalDateTime start = startDate != null
                ? LocalDateTime.parse(startDate)
                : LocalDateTime.now().minusYears(1);

        LocalDateTime end = endDate != null
                ? LocalDateTime.parse(endDate)
                : LocalDateTime.now().plusYears(1);

        dataGeneratorService.generateOrders(businessId, count, start, end);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Successfully generated %d orders from %s to %s", count, start, end)));
    }

    @PostMapping("/generate-all/{businessId}")
    public ResponseEntity<ApiResponse<String>> generateAll(
            @PathVariable UUID businessId,
            @RequestParam(defaultValue = "80000") int orderCount) {

        log.info("Generating statuses and {} orders for business: {}", orderCount, businessId);

        // Generate statuses first
        dataGeneratorService.generateOrderStatuses(businessId);

        // Then generate orders
        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now().plusYears(1);
        dataGeneratorService.generateOrders(businessId, orderCount, start, end);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Successfully generated statuses and %d orders", orderCount)));
    }
}
