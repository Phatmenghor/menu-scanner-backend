package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for users with STUDENT role
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentUserListResponseDto {
    // Basic user info
    private Long id;
    private String username;
    private String email;
    private Status status;

    // Common personal info
    private String khmerFirstName;
    private ClassResponseDto studentClass;
    private String khmerLastName;
    private String englishFirstName;
    private String englishLastName;
    private String profileUrl;
    private GenderEnum gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String identifyNumber;
    private String createdAt;
}
