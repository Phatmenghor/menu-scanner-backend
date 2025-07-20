package com.emenu.features.messaging.models;

import com.emenu.enums.NotificationType;
import com.emenu.enums.UserType;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "message_templates")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MessageTemplate extends BaseUUIDEntity {

    @Column(name = "template_key", nullable = false, unique = true)
    private String templateKey; // e.g., "welcome_email", "subscription_expired"

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType notificationType;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "template_user_types", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "user_type")
    private List<UserType> applicableUserTypes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "language", length = 10)
    private String language = "en";

    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables; // JSON array of available variables

    // Convenience methods
    public String processTemplate(java.util.Map<String, Object> variables) {
        String processedBody = this.body;
        for (java.util.Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            processedBody = processedBody.replace(placeholder, String.valueOf(entry.getValue()));
        }
        return processedBody;
    }

    public String processSubject(java.util.Map<String, Object> variables) {
        String processedSubject = this.subject;
        for (java.util.Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            processedSubject = processedSubject.replace(placeholder, String.valueOf(entry.getValue()));
        }
        return processedSubject;
    }
}