package com.menghor.ksit.feature.setting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for menu item display
 */
/**
 * DTO for menu item display
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDto {
    private Long id;
    private String key;
    private String name;
    private String icon;
    private String route;
    private boolean isParent;
    private boolean isEnabled;
    private Integer displayOrder;

    @Builder.Default
    private List<MenuItemDto> children = new ArrayList<>();
}