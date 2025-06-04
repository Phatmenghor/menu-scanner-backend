package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.feature.school.dto.response.ClassBasicInfoDto;
import lombok.Data;

import java.time.LocalDate;

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
    private String createdAt;
}