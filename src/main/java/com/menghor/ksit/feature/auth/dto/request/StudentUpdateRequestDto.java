package com.menghor.ksit.feature.auth.dto.request;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.dto.relationship.StudentParentDto;
import com.menghor.ksit.feature.auth.dto.relationship.StudentSiblingDto;
import com.menghor.ksit.feature.auth.dto.relationship.StudentStudiesHistoryDto;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentUpdateRequestDto {
    @Email(message = "Email should be valid")
    private String email;

    // Personal info
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
    private Long classId;
    
    // Related entities
    @Builder.Default
    private List<StudentStudiesHistoryDto> studentStudiesHistories = new ArrayList<>();
    
    @Builder.Default
    private List<StudentParentDto> studentParents = new ArrayList<>();
    
    @Builder.Default
    private List<StudentSiblingDto> studentSiblings = new ArrayList<>();

    // Status
    private Status status;
}