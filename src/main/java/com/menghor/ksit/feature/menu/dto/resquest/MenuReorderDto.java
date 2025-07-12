package com.menghor.ksit.feature.menu.dto.resquest;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MenuReorderDto {
    @NotEmpty(message = "Menu item orders are required")
    private List<MenuOrderItem> menuOrders;
    
    @Data
    public static class MenuOrderItem {
        @NotNull(message = "Menu item ID is required")
        private Long menuItemId;
        
        @NotNull(message = "Display order is required")
        private Integer displayOrder;
    }
}