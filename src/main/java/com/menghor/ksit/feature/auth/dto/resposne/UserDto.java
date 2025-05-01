package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;  // Email
    private List<RoleEnum> roles;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Common personal information
    private String firstName;
    private String lastName;
    private String contactNumber;

    // These fields will be populated based on role:
    // For staff/admin/developer
    private String position;
    private String department;
    private String employeeId;

    // For student
    private String studentId;
    private String grade;
    private Integer yearOfAdmission;

    // Helper method to check if this user is a student
    public boolean isStudent() {
        return roles != null && roles.contains(RoleEnum.STUDENT);
    }

    // Helper method to check if this user is staff, admin, or developer
    public boolean isStaffOrAdmin() {
        return roles != null && (
                roles.contains(RoleEnum.STAFF) ||
                        roles.contains(RoleEnum.ADMIN) ||
                        roles.contains(RoleEnum.DEVELOPER)
        );
    }
}