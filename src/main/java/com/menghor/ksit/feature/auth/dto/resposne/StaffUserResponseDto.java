package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.dto.relationship.*;
import com.menghor.ksit.feature.master.dto.response.DepartmentResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for users with roles ADMIN, TEACHER, DEVELOPER, STAFF
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StaffUserResponseDto {
    // Basic user info
    private Long id;
    private String username;
    private String email;
    private List<RoleEnum> roles;
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

    // Staff/Teacher specific fields
    private String identifyNumber;
    private String staffId;
    private String nationalId;
    private LocalDate startWorkDate;
    private LocalDate currentPositionDate;
    private String employeeWork;
    private String disability;
    private String payrollAccountNumber;
    private String cppMembershipNumber;
    private String province;
    private String district;
    private String commune;
    private String village;
    private String officeName;
    private String currentPosition;
    private String decreeFinal;
    private String rankAndClass;

    // Work History
    private String workHistory;

    // Family Information
    private String maritalStatus;
    private String mustBe;
    private String affiliatedProfession;
    private String federationName;
    private String affiliatedOrganization;
    private LocalDate federationEstablishmentDate;
    private String wivesSalary;

    // Teacher Professional History - using DTOs instead of entities
    @Builder.Default
    private List<TeachersProfessionalRankDto> teachersProfessionalRank = new ArrayList<>();

    // Teacher Work Experience
    @Builder.Default
    private List<TeacherExperienceDto> teacherExperience = new ArrayList<>();

    // Teacher Praise or Criticism
    @Builder.Default
    private List<TeacherPraiseOrCriticismDto> teacherPraiseOrCriticism = new ArrayList<>();

    // Teacher Education
    @Builder.Default
    private List<TeacherEducationDto> teacherEducation = new ArrayList<>();

    // Teacher Vocational Training
    @Builder.Default
    private List<TeacherVocationalDto> teacherVocational = new ArrayList<>();

    // Teacher Short Courses
    @Builder.Default
    private List<TeacherShortCourseDto> teacherShortCourse = new ArrayList<>();

    // Teacher Language Skills
    @Builder.Default
    private List<TeacherLanguageDto> teacherLanguage = new ArrayList<>();

    // Teacher Family
    @Builder.Default
    private List<TeacherFamilyDto> teacherFamily = new ArrayList<>();

    // Audit info
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}