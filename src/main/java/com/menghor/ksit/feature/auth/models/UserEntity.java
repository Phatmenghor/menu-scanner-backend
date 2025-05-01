package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.utils.database.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username; // Email address

    private String password;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE; // Default status is ACTIVE

    // Common personal information for all users
    private String firstName;
    private String lastName;
    private String contactNumber;

    // Staff/Admin/Developer fields - used by all non-student roles
    private String position;
    private String department;
    private String employeeId;

    // Student-specific fields - only used for STUDENT role
    private String studentId;
    private String grade;
    private Integer yearOfAdmission;

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(RoleEnum roleEnum) {
        return roles.stream()
                .anyMatch(role -> role.getName() == roleEnum);
    }

    /**
     * Check if user is a student
     */
    public boolean isStudent() {
        return hasRole(RoleEnum.STUDENT);
    }

    /**
     * Check if user is staff, admin, or developer
     */
    public boolean isStaffOrAdmin() {
        return hasRole(RoleEnum.STAFF) || hasRole(RoleEnum.ADMIN) || hasRole(RoleEnum.DEVELOPER);
    }
}