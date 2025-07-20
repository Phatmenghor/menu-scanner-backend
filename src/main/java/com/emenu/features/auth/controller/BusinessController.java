package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.request.BusinessMessageRequest;
import com.emenu.features.auth.dto.request.BusinessStaffCreateRequest;
import com.emenu.features.auth.dto.request.CustomerMessageRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.BusinessStaffResponse;
import com.emenu.features.auth.dto.response.CustomerResponse;
import com.emenu.features.auth.dto.update.BusinessStaffUpdateRequest;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.features.auth.service.BusinessService;
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
@RequestMapping("/api/v1/business")
@RequiredArgsConstructor
@Slf4j
public class BusinessController {

    private final BusinessService businessService;

    // Business Management
    @PostMapping
    public ResponseEntity<ApiResponse<BusinessResponse>> createBusiness(
            @Valid @RequestBody BusinessCreateRequest request) {
        log.info("Creating business: {}", request.getName());
        BusinessResponse business = businessService.createBusiness(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business created successfully", business));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessById(@PathVariable UUID id) {
        log.info("Getting business by ID: {}", id);
        BusinessResponse business = businessService.getBusinessById(id);
        return ResponseEntity.ok(ApiResponse.success("Business retrieved successfully", business));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessResponse>> updateBusiness(
            @PathVariable UUID id,
            @Valid @RequestBody BusinessUpdateRequest request) {
        log.info("Updating business: {}", id);
        BusinessResponse business = businessService.updateBusiness(id, request);
        return ResponseEntity.ok(ApiResponse.success("Business updated successfully", business));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBusiness(@PathVariable UUID id) {
        log.info("Deleting business: {}", id);
        businessService.deleteBusiness(id);
        return ResponseEntity.ok(ApiResponse.success("Business deleted successfully", null));
    }

    // Staff Management
    @PostMapping("/{businessId}/staff")
    public ResponseEntity<ApiResponse<BusinessStaffResponse>> createStaff(
            @PathVariable UUID businessId,
            @Valid @RequestBody BusinessStaffCreateRequest request) {
        log.info("Creating staff for business: {}", businessId);
        BusinessStaffResponse staff = businessService.createStaff(businessId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff member created successfully", staff));
    }

    @GetMapping("/{businessId}/staff")
    public ResponseEntity<ApiResponse<PaginationResponse<BusinessStaffResponse>>> getBusinessStaff(
            @PathVariable UUID businessId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting staff for business: {}", businessId);
        PaginationResponse<BusinessStaffResponse> staff = businessService.getBusinessStaff(businessId, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Business staff retrieved successfully", staff));
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<BusinessStaffResponse>> getStaffById(@PathVariable UUID staffId) {
        log.info("Getting staff by ID: {}", staffId);
        BusinessStaffResponse staff = businessService.getStaffById(staffId);
        return ResponseEntity.ok(ApiResponse.success("Staff member retrieved successfully", staff));
    }

    @PutMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<BusinessStaffResponse>> updateStaff(
            @PathVariable UUID staffId,
            @Valid @RequestBody BusinessStaffUpdateRequest request) {
        log.info("Updating staff: {}", staffId);
        BusinessStaffResponse staff = businessService.updateStaff(staffId, request);
        return ResponseEntity.ok(ApiResponse.success("Staff member updated successfully", staff));
    }

    @DeleteMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable UUID staffId) {
        log.info("Deleting staff: {}", staffId);
        businessService.deleteStaff(staffId);
        return ResponseEntity.ok(ApiResponse.success("Staff member deleted successfully", null));
    }

    @PostMapping("/staff/{staffId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateStaff(@PathVariable UUID staffId) {
        log.info("Activating staff: {}", staffId);
        businessService.activateStaff(staffId);
        return ResponseEntity.ok(ApiResponse.success("Staff member activated successfully", null));
    }

    @PostMapping("/staff/{staffId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateStaff(@PathVariable UUID staffId) {
        log.info("Deactivating staff: {}", staffId);
        businessService.deactivateStaff(staffId);
        return ResponseEntity.ok(ApiResponse.success("Staff member deactivated successfully", null));
    }

    // Business Messaging
    @PostMapping("/{businessId}/messages")
    public ResponseEntity<ApiResponse<Void>> sendBusinessMessage(
            @PathVariable UUID businessId,
            @Valid @RequestBody BusinessMessageRequest request) {
        log.info("Sending business message for business: {}", businessId);
        businessService.sendBusinessMessage(businessId, request);
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully", null));
    }

    @GetMapping("/{businessId}/messages")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageResponse>>> getBusinessMessages(
            @PathVariable UUID businessId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting messages for business: {}", businessId);
        PaginationResponse<MessageResponse> messages = businessService.getBusinessMessages(businessId, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Business messages retrieved successfully", messages));
    }

    @GetMapping("/{businessId}/messages/unread")
    public ResponseEntity<ApiResponse<PaginationResponse<MessageResponse>>> getUnreadMessages(
            @PathVariable UUID businessId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting unread messages for business: {}", businessId);
        PaginationResponse<MessageResponse> messages = businessService.getUnreadMessages(businessId, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Unread messages retrieved successfully", messages));
    }

    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(@PathVariable UUID messageId) {
        log.info("Marking message as read: {}", messageId);
        businessService.markMessageAsRead(messageId);
        return ResponseEntity.ok(ApiResponse.success("Message marked as read", null));
    }

    // Customer Management for Business
    @GetMapping("/{businessId}/customers")
    public ResponseEntity<ApiResponse<PaginationResponse<CustomerResponse>>> getBusinessCustomers(
            @PathVariable UUID businessId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Getting customers for business: {}", businessId);
        PaginationResponse<CustomerResponse> customers = businessService.getBusinessCustomers(businessId, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success("Business customers retrieved successfully", customers));
    }

    @PostMapping("/{businessId}/customers/{customerId}/message")
    public ResponseEntity<ApiResponse<Void>> sendMessageToCustomer(
            @PathVariable UUID businessId,
            @PathVariable UUID customerId,
            @Valid @RequestBody CustomerMessageRequest request) {
        log.info("Sending message to customer: {} from business: {}", customerId, businessId);
        businessService.sendMessageToCustomer(businessId, customerId, request);
        return ResponseEntity.ok(ApiResponse.success("Message sent to customer successfully", null));
    }
}


