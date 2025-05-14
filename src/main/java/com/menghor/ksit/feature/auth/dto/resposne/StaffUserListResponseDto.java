package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.dto.response.DepartmentResponseDto;
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
public class StaffUserListResponseDto {
    private Long id;
    private String username;
    private String email;
    private List<RoleEnum> roles;
    private Status status;
    private DepartmentResponseDto department;
    // Common personal info
    private String khmerFirstName;
    private String khmerLastName;
    private String englishFirstName;
    private String englishLastName;
    private GenderEnum gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String identifyNumber;
    private String staffId;
    private String createdAt;
}
