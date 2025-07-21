package com.emenu.features.notification.service;

import com.emenu.enums.notification.TemplateName;
import com.emenu.features.notification.dto.request.MessageTemplateRequest;
import com.emenu.features.notification.dto.response.MessageTemplateResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MessageTemplateService {
    
    // Template Management
    MessageTemplateResponse createTemplate(MessageTemplateRequest request);
    List<MessageTemplateResponse> getAllTemplates();
    List<MessageTemplateResponse> getActiveTemplates();
    MessageTemplateResponse getTemplateById(UUID templateId);
    MessageTemplateResponse getTemplateByName(TemplateName templateName);
    MessageTemplateResponse updateTemplate(UUID templateId, MessageTemplateRequest request);
    void deleteTemplate(UUID templateId);
    
    // Template Processing
    String processTemplate(TemplateName templateName, Map<String, String> variables);
    String processHtmlTemplate(TemplateName templateName, Map<String, String> variables);
    
    // Template Activation
    void activateTemplate(UUID templateId);
    void deactivateTemplate(UUID templateId);
    
    // Template Seeding
    void seedDefaultTemplates();
    void updateDefaultTemplates();
    
    // Template Validation
    boolean validateTemplate(String templateContent, List<String> requiredVariables);
    List<String> extractVariablesFromTemplate(String templateContent);
}