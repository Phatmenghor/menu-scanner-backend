package com.emenu.features.notification.models;

import com.emenu.enums.notification.TemplateName;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message_templates")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MessageTemplate extends BaseUUIDEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "template_name", nullable = false, unique = true)
    private TemplateName templateName;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables; // JSON array of variable names

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "description")
    private String description;

    @Column(name = "language", length = 2)
    private String language = "en";

    // Business methods
    public String processTemplate(java.util.Map<String, String> variables) {
        String processedContent = this.content;
        if (variables != null) {
            for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
                processedContent = processedContent.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }
        return processedContent;
    }

    public String processHtmlTemplate(java.util.Map<String, String> variables) {
        if (this.htmlContent == null) return null;
        
        String processedContent = this.htmlContent;
        if (variables != null) {
            for (java.util.Map.Entry<String, String> entry : variables.entrySet()) {
                processedContent = processedContent.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }
        return processedContent;
    }
}
