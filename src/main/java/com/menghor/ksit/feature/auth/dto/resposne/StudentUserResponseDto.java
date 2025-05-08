package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.models.StudentParentEntity;
import com.menghor.ksit.feature.auth.models.StudentSiblingEntity;
import com.menghor.ksit.feature.auth.models.StudentStudiesHistoryEntity;
import com.menghor.ksit.feature.master.dto.classes.response.ClassResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for users with STUDENT role
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentUserResponseDto {
    // Basic user info
    private Long id;
    private String username;
    private String email;
    private Status status;

    // Common personal info
    private String khmerFirstName;
    private String khmerLastName;
    private String englishFirstName;
    private String englishLastName;
    private GenderEnum gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String currentAddress;
    private String nationality;
    private String ethnicity;
    private String placeOfBirth;

    // Student-specific fields
    private String memberSiblings;
    private String numberOfSiblings;
    private ClassResponseDto studentClass;
    
    // Student Studies History
    @Builder.Default
    private List<StudentStudiesHistoryEntity> studentStudiesHistory = new ArrayList<>();
    
    // Student Parent Information
    @Builder.Default
    private List<StudentParentEntity> studentParent = new ArrayList<>();
    
    // Student Siblings
    @Builder.Default
    private List<StudentSiblingEntity> studentSibling = new ArrayList<>();

    // Audit info
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}