package com.menghor.ksit.feature.auth.dto.request;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class EnhancedUserUpdateDto {
    @Email(message = "Email should be valid")
    private String username;

    @Email(message = "Email should be valid")
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

    // Staff/Teacher specific fields
    private String staffId;
    private String nationalId;
    private String identifyNumber;
    private LocalDate startWorkDate;
    private LocalDate currentPositionDate;
    private String employeeWork;
    private String disability;
    private String payroll_account_number;
    private String cpp_membership_number;
    private String province;
    private String district;
    private String commune;
    private String village;
    private String officeName;
    private String currentPosition;
    private String decreeFinal;
    private String rankAndClass;
    private Long departmentId;

    // Student-specific fields
    private String memberSiblings;
    private String numberOfSiblings;
    private Long classId;

    private Status status;
}