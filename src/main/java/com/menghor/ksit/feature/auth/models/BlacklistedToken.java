package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "blacklisted_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Column(nullable = false)
    private String username;
}