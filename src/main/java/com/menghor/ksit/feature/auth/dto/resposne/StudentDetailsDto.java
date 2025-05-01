package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentDetailsDto {
    private Long id;
    private String username; // Email
    private String firstName;
    private String lastName;
    private String contactNumber;
    private Status status;

    // Student-specific fields
    private String studentId;
    private String grade;
    private Integer yearOfAdmission;
    
    // Account information
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}