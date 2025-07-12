package com.menghor.ksit.feature.menu.dto.resquest;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuPermissionCreateDto {
    @NotNull(message = "Menu item ID is required")
    private Long menuItemId;
    
    @NotNull(message = "Role is required")
    private RoleEnum role;
    
    private Boolean canView = true;
    private Integer displayOrder = 0;
    private Status status = Status.ACTIVE;
}
