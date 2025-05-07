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
public class UserDetailsDto {
    private Long id;
    private String username;  // Email
    private List<RoleEnum> roles;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper methods to check user type
    public boolean isStudent() {
        return roles != null && roles.contains(RoleEnum.STUDENT);
    }

    public boolean isStaff() {
        return roles != null && roles.contains(RoleEnum.STAFF);
    }

    public boolean isAdmin() {
        return roles != null && roles.contains(RoleEnum.ADMIN);
    }

    public boolean isDeveloper() {
        return roles != null && roles.contains(RoleEnum.DEVELOPER);
    }
}