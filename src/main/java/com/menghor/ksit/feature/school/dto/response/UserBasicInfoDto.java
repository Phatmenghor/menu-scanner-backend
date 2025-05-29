package com.menghor.ksit.feature.school.dto.response;

import lombok.Data;

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
    private ClassBasicInfoDto userClass;
}