package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.request.CustomerCreateRequest;
import com.emenu.features.auth.dto.request.CustomerMessageRequest;
import com.emenu.features.auth.dto.response.CustomerResponse;
import com.emenu.features.auth.dto.update.CustomerUpdateRequest;
import com.emenu.features.auth.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerCreateRequest request) {
        log.info("Creating customer: {}", request.getEmail());
        CustomerResponse customer = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", customer));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<CustomerResponse>>> getCustomers(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        log.info("Getting customers");
        PaginationResponse<CustomerResponse> customers = customerService.getCustomers(pageNo, pageSize, search);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable UUID id) {
        log.info("Getting customer by ID: {}", id);
        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved successfully", customer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerUpdateRequest request) {
        log.info("Updating customer: {}", id);
        CustomerResponse customer = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", customer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable UUID id) {
        log.info("Deleting customer: {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateCustomer(@PathVariable UUID id) {
        log.info("Activating customer: {}", id);
        customerService.activateCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer activated successfully", null));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateCustomer(@PathVariable UUID id) {
        log.info("Deactivating customer: {}", id);
        customerService.deactivateCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deactivated successfully", null));
    }

    // Customer Messaging
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<Void>> sendMessageToCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerMessageRequest request) {
        log.info("Sending message to customer: {}", id);
        customerService.sendMessageToCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully", null));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageResponse>>> getCustomerMessages(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting messages for customer: {}", id);
        PaginationResponse<MessageResponse> messages = customerService.getCustomerMessages(id, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Customer messages retrieved successfully", messages));
    }

    // Customer Self-Service
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> getMyProfile() {
        log.info("Getting my customer profile");
        CustomerResponse customer = customerService.getCurrentCustomerProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", customer));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateMyProfile(
            @Valid @RequestBody CustomerUpdateRequest request) {
        log.info("Updating my customer profile");
        CustomerResponse customer = customerService.updateCurrentCustomerProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", customer));
    }

    @GetMapping("/me/messages")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageResponse>>> getMyMessages(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting my customer messages");
        PaginationResponse<MessageResponse> messages = customerService.getCurrentCustomerMessages(pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved successfully", messages));
    }

    @PostMapping("/me/messages")
    public ResponseEntity<ApiResponse<Void>> sendMessage(@Valid @RequestBody CustomerMessageRequest request) {
        log.info("Sending message from customer");
        customerService.sendMessageFromCustomer(request);
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully", null));
    }

    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(@PathVariable UUID messageId) {
        log.info("Marking message as read: {}", messageId);
        customerService.markMessageAsRead(messageId);
        return ResponseEntity.ok(ApiResponse.success("Message marked as read", null));
    }
}