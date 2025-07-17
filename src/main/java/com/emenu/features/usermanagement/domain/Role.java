package com.emenu.features.usermanagement.domain;

import com.emenu.enums.RoleEnum;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseUUIDEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false)
    private RoleEnum name;

    @Column(name = "description")
    private String description;

    public Role() {}

    public Role(RoleEnum name) {
        this.name = name;
        this.description = name.getDescription();
    }
}