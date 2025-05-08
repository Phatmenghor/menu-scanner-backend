package com.menghor.ksit.feature.auth.dto.resposne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponseDto {
    private long totalUsers;
    private long activeUsers;
    private long totalStudents;
    private long totalStaff;
    private long totalAdmins;
    private long newUsersThisMonth;
    private long newStudentsThisMonth;
}