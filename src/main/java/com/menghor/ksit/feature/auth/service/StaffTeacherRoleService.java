package com.menghor.ksit.feature.auth.service;

import com.menghor.ksit.feature.auth.dto.resposne.UserRoleResponseDto;
import com.menghor.ksit.feature.auth.dto.update.UserRoleUpdateRequestDto;

import java.util.List;

public interface StaffTeacherRoleService {

    /**
     * Get user's STAFF and TEACHER roles - Always returns 2 objects
     * Returns: [{"role": "STAFF", "hasRole": true/false}, {"role": "TEACHER", "hasRole": true/false}]
     */
    List<UserRoleResponseDto> getUserStaffTeacherRoles(Long userId);

    /**
     * Update user's STAFF and TEACHER roles
     * Returns: Always 2 objects with updated hasRole flags
     */
    List<UserRoleResponseDto> updateUserStaffTeacherRoles(Long userId, UserRoleUpdateRequestDto updateDto);
}