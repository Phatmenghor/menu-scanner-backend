package com.menghor.ksit.feature.setting.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for repositioning a menu item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositionMenuItemRequest {
    private String menuKey;
    private PositionType positionType;
    private Integer displayOrder; // Used with SPECIFIC position type
    private String positionAfterItem; // Used with AFTER_ITEM position type
}