package com.menghor.ksit.feature.auth.dto.request;

import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentUpdateDto {
    @Email(message = "Email should be valid")
    private String username;
    
    private String firstName;
    private String lastName;
    private String contactNumber;
    
    // Student-specific fields
    private String studentId;
    private String grade;
    private Integer yearOfAdmission;
    
    // Only admin/staff can update these
    private Status status;
}