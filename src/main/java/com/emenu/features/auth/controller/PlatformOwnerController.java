package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import com.emenu.features.auth.dto.filter.UserFilterRequest;
import com.emenu.features.auth.dto.request.PlatformMessageRequest;
import com.emenu.features.auth.dto.request.PlatformUserCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.CustomerResponse;
import com.emenu.features.auth.dto.response.PlatformUserResponse;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.features.auth.dto.update.CustomerUpdateRequest;
import com.emenu.features.auth.dto.update.PlatformUserUpdateRequest;
import com.emenu.features.auth.service.PlatformOwnerService;
import com.emenu.features.messaging.dto.filter.MessageFilterRequest;
import com.emenu.features.messaging.dto.response.MessageResponse;
import com.emenu.shared.dto.ApiResponse;
import com.emenu.shared.dto.PaginationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/platform")
@RequiredArgsConstructor
@Slf4j
public class PlatformOwnerController {

    private final PlatformOwnerService platformOwnerService;

    // Platform User Management
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<PlatformUserResponse>> createPlatformUser(
            @Valid @RequestBody PlatformUserCreateRequest request) {
        log.info("Creating platform user: {}", request.getEmail());
        PlatformUserResponse user = platformOwnerService.createPlatformUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Platform user created successfully", user));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PaginationResponse<PlatformUserResponse>>> getAllPlatformUsers(
            @ModelAttribute UserFilterRequest filter) {
        log.info("Getting all platform users");
        PaginationResponse<PlatformUserResponse> users = platformOwnerService.getAllPlatformUsers(filter);
        return ResponseEntity.ok(ApiResponse.success("Platform users retrieved successfully", users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<PlatformUserResponse>> getPlatformUserById(@PathVariable UUID id) {
        log.info("Getting platform user by ID: {}", id);
        PlatformUserResponse user = platformOwnerService.getPlatformUserById(id);
        return ResponseEntity.ok(ApiResponse.success("Platform user retrieved successfully", user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<PlatformUserResponse>> updatePlatformUser(
            @PathVariable UUID id,
            @Valid @RequestBody PlatformUserUpdateRequest request) {
        log.info("Updating platform user: {}", id);
        PlatformUserResponse user = platformOwnerService.updatePlatformUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Platform user updated successfully", user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlatformUser(@PathVariable UUID id) {
        log.info("Deleting platform user: {}", id);
        platformOwnerService.deletePlatformUser(id);
        return ResponseEntity.ok(ApiResponse.success("Platform user deleted successfully", null));
    }

    // All Business Management
    @GetMapping("/businesses")
    public ResponseEntity<ApiResponse<PaginationResponse<BusinessResponse>>> getAllBusinesses(
            @ModelAttribute BusinessFilterRequest filter) {
        log.info("Getting all businesses");
        PaginationResponse<BusinessResponse> businesses = platformOwnerService.getAllBusinesses(filter);
        return ResponseEntity.ok(ApiResponse.success("Businesses retrieved successfully", businesses));
    }

    @GetMapping("/businesses/{id}")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessById(@PathVariable UUID id) {
        log.info("Getting business by ID: {}", id);
        BusinessResponse business = platformOwnerService.getBusinessById(id);
        return ResponseEntity.ok(ApiResponse.success("Business retrieved successfully", business));
    }

    @PutMapping("/businesses/{id}")
    public ResponseEntity<ApiResponse<BusinessResponse>> updateBusiness(
            @PathVariable UUID id,
            @Valid @RequestBody BusinessUpdateRequest request) {
        log.info("Updating business: {}", id);
        BusinessResponse business = platformOwnerService.updateBusiness(id, request);
        return ResponseEntity.ok(ApiResponse.success("Business updated successfully", business));
    }

    @DeleteMapping("/businesses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBusiness(@PathVariable UUID id) {
        log.info("Deleting business: {}", id);
        platformOwnerService.deleteBusiness(id);
        return ResponseEntity.ok(ApiResponse.success("Business deleted successfully", null));
    }

    @PostMapping("/businesses/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendBusiness(@PathVariable UUID id) {
        log.info("Suspending business: {}", id);
        platformOwnerService.suspendBusiness(id);
        return ResponseEntity.ok(ApiResponse.success("Business suspended successfully", null));
    }

    @PostMapping("/businesses/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateBusiness(@PathVariable UUID id) {
        log.info("Activating business: {}", id);
        platformOwnerService.activateBusiness(id);
        return ResponseEntity.ok(ApiResponse.success("Business activated successfully", null));
    }

    // All Customer Management
    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<PaginationResponse<CustomerResponse>>> getAllCustomers(
            @ModelAttribute UserFilterRequest filter) {
        log.info("Getting all customers");
        PaginationResponse<CustomerResponse> customers = platformOwnerService.getAllCustomers(filter);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customers));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable UUID id) {
        log.info("Getting customer by ID: {}", id);
        CustomerResponse customer = platformOwnerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", customer));
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerUpdateRequest request) {
        log.info("Updating customer: {}", id);
        CustomerResponse customer = platformOwnerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", customer));
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable UUID id) {
        log.info("Deleting customer: {}", id);
        platformOwnerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }

    // Platform Messaging
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<Void>> sendPlatformMessage(
            @Valid @RequestBody PlatformMessageRequest request) {
        log.info("Sending platform message");
        platformOwnerService.sendPlatformMessage(request);
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully", null));
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageResponse>>> getAllMessages(
            @ModelAttribute MessageFilterRequest filter) {
        log.info("Getting all platform messages");
        PaginationResponse<MessageResponse> messages = platformOwnerService.getAllMessages(filter);
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved successfully", messages));
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> getMessageById(@PathVariable UUID id) {
        log.info("Getting message by ID: {}", id);
        MessageResponse message = platformOwnerService.getMessageById(id);
        return ResponseEntity.ok(ApiResponse.success("Message retrieved successfully", message));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable UUID id) {
        log.info("Deleting message: {}", id);
        platformOwnerService.deleteMessage(id);
        return ResponseEntity.ok(ApiResponse.success("Message deleted successfully", null));
    }

    // System Management
    @PostMapping("/users/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable UUID id) {
        log.info("Locking user: {}", id);
        platformOwnerService.lockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User locked successfully", null));
    }

    @PostMapping("/users/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable UUID id) {
        log.info("Unlocking user: {}", id);
        platformOwnerService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User unlocked successfully", null));
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetUserPassword(@PathVariable UUID id) {
        log.info("Resetting password for user: {}", id);
        platformOwnerService.resetUserPassword(id);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}