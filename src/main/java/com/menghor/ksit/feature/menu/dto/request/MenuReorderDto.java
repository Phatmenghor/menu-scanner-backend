package com.menghor.ksit.feature.menu.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MenuReorderDto {
    @NotNull(message = "Menu ID is required")
    private Long menuId;
    
    @NotNull(message = "New display order is required")
    @Positive(message = "Display order must be positive")
    private Integer newDisplayOrder;
}
