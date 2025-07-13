package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleResponseDto {
    private RoleEnum role;      // "STAFF" or "TEACHER"
    private String name;        // "Staff" or "Teacher" 
    private Boolean hasRole;    // true if user has this role, false if not
}