package com.emenu.shared.audit;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_endpoint", columnList = "endpoint"),
        @Index(name = "idx_audit_ip", columnList = "ip_address"),
        @Index(name = "idx_audit_created", columnList = "created_at"),
        @Index(name = "idx_audit_status", columnList = "status_code")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseUUIDEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_identifier", length = 255)
    private String userIdentifier;

    @Column(name = "user_type", length = 50)
    private String userType;

    @Column(name = "http_method", length = 10, nullable = false)
    private String httpMethod;

    @Column(name = "endpoint", length = 500, nullable = false)
    private String endpoint;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "session_id", length = 100)
    private String sessionId;
}
