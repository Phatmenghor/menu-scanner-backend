package com.emenu.features.notification.controller;

import com.emenu.enums.notification.AlertType;
import com.emenu.features.notification.service.AlertService;
import com.emenu.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;

    /**
     * Send subscription expiring alert
     */
    @PostMapping("/subscription-expiring/{businessId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendSubscriptionExpiringAlert(
            @PathVariable UUID businessId,
            @RequestParam int daysRemaining) {
        log.info("Sending subscription expiring alert for business: {}, days: {}", businessId, daysRemaining);
        alertService.sendSubscriptionExpiringAlert(businessId, daysRemaining);
        return ResponseEntity.ok(ApiResponse.success("Subscription expiring alert sent successfully", null));
    }

    /**
     * Send subscription expired alert
     */
    @PostMapping("/subscription-expired/{businessId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendSubscriptionExpiredAlert(@PathVariable UUID businessId) {
        log.info("Sending subscription expired alert for business: {}", businessId);
        alertService.sendSubscriptionExpiredAlert(businessId);
        return ResponseEntity.ok(ApiResponse.success("Subscription expired alert sent successfully", null));
    }

    /**
     * Send payment reminder alert
     */
    @PostMapping("/payment-reminder/{businessId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendPaymentReminderAlert(@PathVariable UUID businessId) {
        log.info("Sending payment reminder alert for business: {}", businessId);
        alertService.sendPaymentReminderAlert(businessId);
        return ResponseEntity.ok(ApiResponse.success("Payment reminder alert sent successfully", null));
    }

    /**
     * Send custom alert
     */
    @PostMapping("/custom")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendCustomAlert(
            @RequestParam UUID recipientId,
            @RequestParam AlertType alertType,
            @RequestParam String title,
            @RequestParam String message) {
        log.info("Sending custom alert to: {}", recipientId);
        alertService.sendCustomAlert(recipientId, alertType, title, message);
        return ResponseEntity.ok(ApiResponse.success("Custom alert sent successfully", null));
    }

    /**
     * Send bulk alert
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendBulkAlert(
            @RequestParam List<UUID> recipientIds,
            @RequestParam AlertType alertType,
            @RequestParam String title,
            @RequestParam String message) {
        log.info("Sending bulk alert to {} recipients", recipientIds.size());
        alertService.sendBulkAlert(recipientIds, alertType, title, message);
        return ResponseEntity.ok(ApiResponse.success("Bulk alert sent successfully", null));
    }

    /**
     * Enable alert for user
     */
    @PostMapping("/enable")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> enableAlert(
            @RequestParam UUID userId,
            @RequestParam AlertType alertType) {
        log.info("Enabling alert {} for user: {}", alertType, userId);
        alertService.enableAlert(userId, alertType);
        return ResponseEntity.ok(ApiResponse.success("Alert enabled successfully", null));
    }

    /**
     * Disable alert for user
     */
    @PostMapping("/disable")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> disableAlert(
            @RequestParam UUID userId,
            @RequestParam AlertType alertType) {
        log.info("Disabling alert {} for user: {}", alertType, userId);
        alertService.disableAlert(userId, alertType);
        return ResponseEntity.ok(ApiResponse.success("Alert disabled successfully", null));
    }

    /**
     * Check if alert is enabled for user
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT', 'BUSINESS_OWNER', 'BUSINESS_MANAGER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Boolean>> isAlertEnabled(
            @RequestParam UUID userId,
            @RequestParam AlertType alertType) {
        log.info("Checking if alert {} is enabled for user: {}", alertType, userId);
        boolean enabled = alertService.isAlertEnabled(userId, alertType);
        return ResponseEntity.ok(ApiResponse.success("Alert status retrieved successfully", enabled));
    }

    /**
     * Trigger subscription alerts check
     */
    @PostMapping("/check-subscriptions")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> checkSubscriptionAlerts() {
        log.info("Triggering subscription alerts check");
        alertService.checkAndSendSubscriptionAlerts();
        return ResponseEntity.ok(ApiResponse.success("Subscription alerts check completed", null));
    }

    /**
     * Trigger payment alerts check
     */
    @PostMapping("/check-payments")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> checkPaymentAlerts() {
        log.info("Triggering payment alerts check");
        alertService.checkAndSendPaymentAlerts();
        return ResponseEntity.ok(ApiResponse.success("Payment alerts check completed", null));
    }

    /**
     * Send system maintenance alert
     */
    @PostMapping("/system-maintenance")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendSystemMaintenanceAlert() {
        log.info("Sending system maintenance alert");
        alertService.sendSystemMaintenanceAlert();
        return ResponseEntity.ok(ApiResponse.success("System maintenance alert sent successfully", null));
    }
}