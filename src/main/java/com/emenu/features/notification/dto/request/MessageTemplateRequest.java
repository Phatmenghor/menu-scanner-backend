package com.emenu.features.notification.dto.request;

import com.emenu.enums.notification.TemplateName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MessageTemplateRequest {
    
    @NotNull(message = "Template name is required")
    private TemplateName templateName;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String htmlContent;
    private List<String> variables;
    private String description;
    private String language = "en";
    private Boolean isActive = true;
}
