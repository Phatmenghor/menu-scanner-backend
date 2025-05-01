package com.menghor.ksit.feature.auth.dto.request;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserUpdateDto {
    @Email(message = "Email should be valid")
    private String username;

    // Single role for backward compatibility
    private RoleEnum role;

    // Multiple roles support
    private List<RoleEnum> roles;

    private Status status;

    // Common personal information
    private String firstName;
    private String lastName;
    private String contactNumber;

    // Student-specific information
    private String studentId;
    private String grade;
    private Integer yearOfAdmission;

    // Staff-specific information
    private String position;
    private String department;
    private String employeeId;
}