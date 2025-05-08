package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.dto.classes.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.department.response.DepartmentResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsResponseDto {
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
    private DepartmentResponseDto department;

    // Student-specific fields
    private String memberSiblings;
    private String numberOfSiblings;
    private ClassResponseDto studentClass;

    // Audit info
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}