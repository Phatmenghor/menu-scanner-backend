package com.menghor.ksit.feature.menu.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserMenuUpdateDto {
    @Valid
    @NotNull(message = "Menu permissions are required")
    private List<MenuPermissionUpdateDto> menuPermissions;
}