package com.menghor.ksit.feature.setting.dto.request;

import com.menghor.ksit.enumations.PositionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new menu item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuItemRequest {
    private String menuKey;
    private String menuName;
    private String parentKey;
    private boolean parent;
    private String icon;
    private String route;
    private Integer displayOrder;
    private PositionType positionType = PositionType.SPECIFIC; // Default to specific position
}