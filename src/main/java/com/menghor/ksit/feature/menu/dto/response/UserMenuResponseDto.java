package com.menghor.ksit.feature.menu.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserMenuResponseDto {
    private Long id;
    private String code;
    private String title;
    private String route;
    private String icon;
    private Integer displayOrder;
    private Boolean isParent;
    private Long parentId;
    private String parentTitle;
    
    // Permission fields
    private Boolean canView = false;
    private Boolean isCustomized = false; // Whether user has custom settings
    
    // Children menus
    private List<UserMenuResponseDto> children;
    
    private LocalDateTime createdAt;
}