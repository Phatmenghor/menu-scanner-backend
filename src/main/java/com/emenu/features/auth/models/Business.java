package com.emenu.features.auth.models;

import com.emenu.enums.BusinessStatus;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "businesses")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Business extends BaseUUIDEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BusinessStatus status = BusinessStatus.ACTIVE;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<User> users;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    private List<Subscription> subscriptions;

    public boolean isActive() {
        return BusinessStatus.ACTIVE.equals(status);
    }
}
