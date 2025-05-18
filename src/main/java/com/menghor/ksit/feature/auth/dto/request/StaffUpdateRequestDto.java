package com.menghor.ksit.feature.auth.dto.request;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.dto.relationship.*;
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
public class StaffUpdateRequestDto {

    private String username;

    private String email;
    // Multiple roles support
    private List<RoleEnum> roles;

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
    private String profileUrl;

    private String taughtEnglish;
    private String threeLevelClass;
    private String referenceNote;
    private String technicalTeamLeader;
    private String assistInTeaching;
    private String serialNumber;
    private String twoLevelClass;
    private String classResponsibility;
    private LocalDate lastSalaryIncrementDate;
    private String teachAcrossSchools;
    private String overtimeHours;
    private LocalDate issuedDate;
    private String suitableClass;
    private String bilingual;
    private String academicYearTaught;
    private String workHistory;

    // Staff/Teacher specific fields
    private String staffId;
    private String nationalId;
    private String identifyNumber;
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
    private Long departmentId;
    
    // Family Information
    private String maritalStatus;
    private String mustBe;
    private String affiliatedProfession;
    private String federationName;
    private String affiliatedOrganization;
    private LocalDate federationEstablishmentDate;
    private String wivesSalary;
    
    // Related entities
    @Builder.Default
    private List<TeachersProfessionalRankDto> teachersProfessionalRanks = new ArrayList<>();
    
    @Builder.Default
    private List<TeacherExperienceDto> teacherExperiences = new ArrayList<>();
    
    @Builder.Default
    private List<TeacherPraiseOrCriticismDto> teacherPraiseOrCriticisms = new ArrayList<>();
    
    @Builder.Default
    private List<TeacherEducationDto> teacherEducations = new ArrayList<>();
    
    @Builder.Default
    private List<TeacherVocationalDto> teacherVocationals = new ArrayList<>();
    
    @Builder.Default
    private List<TeacherShortCourseDto> teacherShortCourses = new ArrayList<>();
    
    @Builder.Default
    private List<TeacherLanguageDto> teacherLanguages = new ArrayList<>();
    
    @Builder.Default
    private List<TeacherFamilyDto> teacherFamilies = new ArrayList<>();

    // Status
    private Status status;
}