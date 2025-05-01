package com.menghor.ksit.utils.database;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false,name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = true, name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}