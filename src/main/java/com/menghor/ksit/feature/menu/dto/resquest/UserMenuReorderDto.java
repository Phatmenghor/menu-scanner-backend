package com.menghor.ksit.feature.menu.dto.resquest;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserMenuReorderDto {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotEmpty(message = "Menu item orders are required")
    private List<UserMenuOrderItem> menuOrders;
    
    @Data
    public static class UserMenuOrderItem {
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;
        
        @NotNull(message = "Display order is required")
        private Integer displayOrder;
        
        private Boolean canView = true;
    }
}