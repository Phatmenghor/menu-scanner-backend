package com.emenu.features.notification.controller;

import com.emenu.enums.notification.TemplateName;
import com.emenu.features.notification.dto.request.MessageTemplateRequest;
import com.emenu.features.notification.dto.response.MessageTemplateResponse;
import com.emenu.features.notification.service.MessageTemplateService;
import com.emenu.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/message-templates")
@RequiredArgsConstructor
@Slf4j
public class MessageTemplateController {

    private final MessageTemplateService templateService;

    /**
     * Create a message template
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<MessageTemplateResponse>> createTemplate(@Valid @RequestBody MessageTemplateRequest request) {
        log.info("Creating message template: {}", request.getTemplateName());
        MessageTemplateResponse template = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message template created successfully", template));
    }

    /**
     * Get all templates
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT')")
    public ResponseEntity<ApiResponse<List<MessageTemplateResponse>>> getAllTemplates() {
        log.info("Getting all message templates");
        List<MessageTemplateResponse> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success("Message templates retrieved successfully", templates));
    }

    /**
     * Get active templates
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT')")
    public ResponseEntity<ApiResponse<List<MessageTemplateResponse>>> getActiveTemplates() {
        log.info("Getting active message templates");
        List<MessageTemplateResponse> templates = templateService.getActiveTemplates();
        return ResponseEntity.ok(ApiResponse.success("Active message templates retrieved successfully", templates));
    }

    /**
     * Get template by ID
     */
    @GetMapping("/{templateId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT')")
    public ResponseEntity<ApiResponse<MessageTemplateResponse>> getTemplateById(@PathVariable UUID templateId) {
        log.info("Getting message template by ID: {}", templateId);
        MessageTemplateResponse template = templateService.getTemplateById(templateId);
        return ResponseEntity.ok(ApiResponse.success("Message template retrieved successfully", template));
    }

    /**
     * Get template by name
     */
    @GetMapping("/by-name/{templateName}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT')")
    public ResponseEntity<ApiResponse<MessageTemplateResponse>> getTemplateByName(@PathVariable TemplateName templateName) {
        log.info("Getting message template by name: {}", templateName);
        MessageTemplateResponse template = templateService.getTemplateByName(templateName);
        return ResponseEntity.ok(ApiResponse.success("Message template retrieved successfully", template));
    }

    /**
     * Update template
     */
    @PutMapping("/{templateId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<MessageTemplateResponse>> updateTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody MessageTemplateRequest request) {
        log.info("Updating message template: {}", templateId);
        MessageTemplateResponse template = templateService.updateTemplate(templateId, request);
        return ResponseEntity.ok(ApiResponse.success("Message template updated successfully", template));
    }

    /**
     * Delete template
     */
    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID templateId) {
        log.info("Deleting message template: {}", templateId);
        templateService.deleteTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.success("Message template deleted successfully", null));
    }

    /**
     * Activate template
     */
    @PostMapping("/{templateId}/activate")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateTemplate(@PathVariable UUID templateId) {
        log.info("Activating message template: {}", templateId);
        templateService.activateTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.success("Message template activated successfully", null));
    }

    /**
     * Deactivate template
     */
    @PostMapping("/{templateId}/deactivate")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateTemplate(@PathVariable UUID templateId) {
        log.info("Deactivating message template: {}", templateId);
        templateService.deactivateTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.success("Message template deactivated successfully", null));
    }

    /**
     * Process template with variables
     */
    @PostMapping("/{templateName}/process")
    @PreAuthorize("hasAnyRole('PLATFORM_OWNER', 'PLATFORM_ADMIN', 'PLATFORM_SUPPORT')")
    public ResponseEntity<ApiResponse<String>> processTemplate(
            @PathVariable TemplateName templateName,
            @RequestBody Map<String, String> variables) {
        log.info("Processing template: {} with variables", templateName);
        String processedContent = templateService.processTemplate(templateName, variables);
        return ResponseEntity.ok(ApiResponse.success("Template processed successfully", processedContent));
    }

    /**
     * Seed default templates
     */
    @PostMapping("/seed")
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<Void>> seedDefaultTemplates() {
        log.info("Seeding default message templates");
        templateService.seedDefaultTemplates();
        return ResponseEntity.ok(ApiResponse.success("Default templates seeded successfully", null));
    }

    /**
     * Update default templates
     */
    @PostMapping("/update-defaults")
    @PreAuthorize("hasRole('PLATFORM_OWNER')")
    public ResponseEntity<ApiResponse<Void>> updateDefaultTemplates() {
        log.info("Updating default message templates");
        templateService.updateDefaultTemplates();
        return ResponseEntity.ok(ApiResponse.success("Default templates updated successfully", null));
    }
}