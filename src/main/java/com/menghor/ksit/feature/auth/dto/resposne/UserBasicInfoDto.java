package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.school.dto.response.ClassBasicInfoDto;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserBasicInfoDto {
    private Long id;
    private String username;
    private String khmerFirstName;
    private String khmerLastName;
    private String englishFirstName;
    private String englishLastName;
    private String email;
    private String phoneNumber;
    private String identifyNumber;
    private String degree;
    private LocalDate dateOfBirth;
    private GenderEnum gender;
    private String currentAddress;
    private String profileUrl;
    private String majorName;
    private String departmentName;
    private ClassBasicInfoDto userClass;
    private List<RoleEnum> roles;
    private Boolean isStudent;
    private String createdAt;
}