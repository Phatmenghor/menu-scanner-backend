package com.menghor.ksit.feature.menu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuCreateDto {
    @NotNull(message = "Menu code is required")
    private String code;
    
    @NotNull(message = "Menu title is required")
    private String title;
    
    private String route;
    private String icon;
    private Boolean isParent = false;
    private Long parentId;
    private Integer displayOrder;
}