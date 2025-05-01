package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private RoleEnum userRole;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}