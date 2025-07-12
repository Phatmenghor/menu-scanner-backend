package com.menghor.ksit.feature.menu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuPermissionUpdateDto {
    @NotNull(message = "Menu ID is required")
    private Long menuId;
    
    private Boolean canView = false;
}
