package com.emenu.features.auth.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseUUIDEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private RoleEnum name;

    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    public Role(RoleEnum name) {
        this.name = name;
        this.description = name.getDescription();
    }

    public boolean isPlatformRole() {
        return name.isPlatformRole();
    }

    public boolean isBusinessRole() {
        return name.isBusinessRole();
    }

    public boolean isCustomerRole() {
        return name.isCustomerRole();
    }
}