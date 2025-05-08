package com.menghor.ksit.feature.auth.dto.request;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import lombok.Data;

import java.util.List;

@Data
public class UserFilterRequestDto {
    private String search;
    private Status status;
    private List<RoleEnum> roles;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}