package com.menghor.ksit.feature.menu.dto.resquest;

import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserMenuPermissionCreateDto {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Menu item ID is required")
    private Long menuItemId;
    
    private Boolean canView = true;
    private Integer displayOrder = 0;
    private String customTitle;
    private String customIcon;
    private Status status = Status.ACTIVE;
}