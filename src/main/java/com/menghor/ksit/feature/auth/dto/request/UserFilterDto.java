package com.menghor.ksit.feature.auth.dto.request;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class UserFilterDto {
    private String search;
    private Status status;
    private RoleEnum role;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}