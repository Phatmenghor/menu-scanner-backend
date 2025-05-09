package com.menghor.ksit.feature.auth.dto.request;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StaffRegisterRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must have at least 6 characters")
    private String password;

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

    // Status with default value
    private Status status = Status.ACTIVE;
}