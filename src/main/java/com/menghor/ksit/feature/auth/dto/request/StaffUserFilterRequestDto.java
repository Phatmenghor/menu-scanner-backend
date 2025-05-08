package com.menghor.ksit.feature.auth.dto.request;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import lombok.Data;

import java.util.List;

/**
 * Filter DTO for staff users (admin, teacher, developer, staff)
 */
@Data
public class StaffUserFilterRequestDto {
    private String search;
    private Status status;
    private List<RoleEnum> roles;
    private Long departmentId;
    private String position;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}