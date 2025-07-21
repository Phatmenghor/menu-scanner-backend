package com.emenu.features.notification.dto.response;

import com.emenu.enums.notification.TemplateName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class MessageTemplateResponse {
    
    private UUID id;
    private TemplateName templateName;
    private String subject;
    private String content;
    private String htmlContent;
    private List<String> variables;
    private Boolean isActive;
    private String description;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}