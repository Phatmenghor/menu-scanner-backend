package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.dto.relationship.StudentParentDto;
import com.menghor.ksit.feature.auth.dto.relationship.StudentSiblingDto;
import com.menghor.ksit.feature.auth.dto.relationship.StudentStudiesHistoryDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
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

    // Student Studies History - using DTOs instead of entities
    @Builder.Default
    private List<StudentStudiesHistoryDto> studentStudiesHistory = new ArrayList<>();

    // Student Parent Information
    @Builder.Default
    private List<StudentParentDto> studentParent = new ArrayList<>();

    // Student Siblings
    @Builder.Default
    private List<StudentSiblingDto> studentSibling = new ArrayList<>();

    // Audit info
    private LocalDateTime createdAt;
}