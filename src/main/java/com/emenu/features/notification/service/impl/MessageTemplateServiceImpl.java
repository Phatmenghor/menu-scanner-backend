package com.emenu.features.notification.service.impl;

import com.emenu.enums.notification.TemplateName;
import com.emenu.features.notification.dto.request.MessageTemplateRequest;
import com.emenu.features.notification.dto.response.MessageTemplateResponse;
import com.emenu.features.notification.mapper.MessageTemplateMapper;
import com.emenu.features.notification.models.MessageTemplate;
import com.emenu.features.notification.repository.MessageTemplateRepository;
import com.emenu.features.notification.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageTemplateServiceImpl implements MessageTemplateService {

    private final MessageTemplateRepository templateRepository;
    private final MessageTemplateMapper templateMapper;

    @Override
    public MessageTemplateResponse createTemplate(MessageTemplateRequest request) {
        log.info("Creating message template: {}", request.getTemplateName());

        if (templateRepository.existsByTemplateNameAndIsDeletedFalse(request.getTemplateName())) {
            throw new RuntimeException("Template with this name already exists");
        }

        MessageTemplate template = templateMapper.toEntity(request);
        MessageTemplate savedTemplate = templateRepository.save(template);

        log.info("Message template created successfully: {}", savedTemplate.getTemplateName());
        return templateMapper.toResponse(savedTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageTemplateResponse> getAllTemplates() {
        List<MessageTemplate> templates = templateRepository.findAllActiveTemplates();
        return templateMapper.toResponseList(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageTemplateResponse> getActiveTemplates() {
        List<MessageTemplate> templates = templateRepository.findByIsActiveAndIsDeletedFalse(true);
        return templateMapper.toResponseList(templates);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageTemplateResponse getTemplateById(UUID templateId) {
        MessageTemplate template = templateRepository.findByIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> new RuntimeException("Message template not found"));

        return templateMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageTemplateResponse getTemplateByName(TemplateName templateName) {
        MessageTemplate template = templateRepository.findByTemplateNameAndIsDeletedFalse(templateName)
                .orElseThrow(() -> new RuntimeException("Message template not found: " + templateName));

        return templateMapper.toResponse(template);
    }

    @Override
    public MessageTemplateResponse updateTemplate(UUID templateId, MessageTemplateRequest request) {
        MessageTemplate template = templateRepository.findByIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> new RuntimeException("Message template not found"));

        templateMapper.updateEntity(request, template);
        MessageTemplate updatedTemplate = templateRepository.save(template);

        log.info("Message template updated successfully: {}", updatedTemplate.getTemplateName());
        return templateMapper.toResponse(updatedTemplate);
    }

    @Override
    public void deleteTemplate(UUID templateId) {
        MessageTemplate template = templateRepository.findByIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> new RuntimeException("Message template not found"));

        template.softDelete();
        templateRepository.save(template);
        log.info("Message template deleted: {}", template.getTemplateName());
    }

    @Override
    @Transactional(readOnly = true)
    public String processTemplate(TemplateName templateName, Map<String, String> variables) {
        MessageTemplate template = templateRepository.findByTemplateNameAndIsDeletedFalse(templateName)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateName));

        return template.processTemplate(variables);
    }

    @Override
    @Transactional(readOnly = true)
    public String processHtmlTemplate(TemplateName templateName, Map<String, String> variables) {
        MessageTemplate template = templateRepository.findByTemplateNameAndIsDeletedFalse(templateName)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateName));

        return template.processHtmlTemplate(variables);
    }

    @Override
    public void activateTemplate(UUID templateId) {
        MessageTemplate template = templateRepository.findByIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> new RuntimeException("Message template not found"));

        template.setIsActive(true);
        templateRepository.save(template);
        log.info("Message template activated: {}", template.getTemplateName());
    }

    @Override
    public void deactivateTemplate(UUID templateId) {
        MessageTemplate template = templateRepository.findByIdAndIsDeletedFalse(templateId)
                .orElseThrow(() -> new RuntimeException("Message template not found"));

        template.setIsActive(false);
        templateRepository.save(template);
        log.info("Message template deactivated: {}", template.getTemplateName());
    }

    @Override
    public void seedDefaultTemplates() {
        log.info("Seeding default message templates");

        if (templateRepository.count() > 0) {
            log.info("Templates already exist, skipping seed");
            return;
        }

        // Subscription Expiry Warning Template
        createDefaultTemplate(
            TemplateName.SUBSCRIPTION_EXPIRY_WARNING,
            "Subscription Expiring Soon",
            "Dear {{businessName}},\n\nYour subscription will expire in {{daysRemaining}} days on {{expiryDate}}. Please renew to continue using our services.\n\nBest regards,\nE-Menu Platform Team",
            "<p>Dear <strong>{{businessName}}</strong>,</p><p>Your subscription will expire in <strong>{{daysRemaining}} days</strong> on {{expiryDate}}. Please renew to continue using our services.</p><p>Best regards,<br>E-Menu Platform Team</p>",
            List.of("businessName", "daysRemaining", "expiryDate")
        );

        // Subscription Expired Template
        createDefaultTemplate(
            TemplateName.SUBSCRIPTION_EXPIRED,
            "Subscription Expired",
            "Dear {{businessName}},\n\nYour subscription has expired on {{expiryDate}}. Please renew immediately to restore access to all features.\n\nBest regards,\nE-Menu Platform Team",
            "<p>Dear <strong>{{businessName}}</strong>,</p><p>Your subscription has expired on {{expiryDate}}. Please renew immediately to restore access to all features.</p><p>Best regards,<br>E-Menu Platform Team</p>",
            List.of("businessName", "expiryDate")
        );

        // Welcome User Template
        createDefaultTemplate(
            TemplateName.WELCOME_USER,
            "Welcome to E-Menu Platform",
            "Dear {{userName}},\n\nWelcome to E-Menu Platform! We're excited to have you on board.\n\nGet started by exploring our features and setting up your account.\n\nBest regards,\nE-Menu Platform Team",
            "<p>Dear <strong>{{userName}}</strong>,</p><p>Welcome to E-Menu Platform! We're excited to have you on board.</p><p>Get started by exploring our features and setting up your account.</p><p>Best regards,<br>E-Menu Platform Team</p>",
            List.of("userName")
        );

        // Password Reset Template
        createDefaultTemplate(
            TemplateName.PASSWORD_RESET,
            "Password Reset Request",
            "You have requested a password reset for your E-Menu Platform account.\n\nClick the following link to reset your password:\n{{resetLink}}\n\nIf you didn't request this, please ignore this email.\n\nBest regards,\nE-Menu Platform Team",
            "<p>You have requested a password reset for your E-Menu Platform account.</p><p><a href=\"{{resetLink}}\">Click here to reset your password</a></p><p>If you didn't request this, please ignore this email.</p><p>Best regards,<br>E-Menu Platform Team</p>",
            List.of("resetLink")
        );

        log.info("Default message templates seeded successfully");
    }

    @Override
    public void updateDefaultTemplates() {
        log.info("Updating default message templates");
        // Implementation for updating existing default templates
    }

    @Override
    public boolean validateTemplate(String templateContent, List<String> requiredVariables) {
        if (templateContent == null || templateContent.trim().isEmpty()) {
            return false;
        }

        // Extract variables from template
        List<String> templateVariables = extractVariablesFromTemplate(templateContent);

        // Check if all required variables are present
        return templateVariables.containsAll(requiredVariables);
    }

    @Override
    public List<String> extractVariablesFromTemplate(String templateContent) {
        Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(templateContent);
        
        return matcher.results()
                .map(matchResult -> matchResult.group(1).trim())
                .distinct()
                .collect(Collectors.toList());
    }

    private void createDefaultTemplate(TemplateName templateName, String subject, String content, 
                                     String htmlContent, List<String> variables) {
        if (templateRepository.existsByTemplateNameAndIsDeletedFalse(templateName)) {
            return; // Template already exists
        }

        MessageTemplate template = new MessageTemplate();
        template.setTemplateName(templateName);
        template.setSubject(subject);
        template.setContent(content);
        template.setHtmlContent(htmlContent);
        template.setVariables(String.join(",", variables));
        template.setIsActive(true);
        template.setLanguage("en");
        template.setDescription("Default system template");

        templateRepository.save(template);
        log.debug("Created default template: {}", templateName);
    }
}
