package com.menghor.ksit.feature.setting.dto.request;

import com.menghor.ksit.enumations.PositionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a menu item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuItemRequest {
    private String menuKey;
    private String menuName;
    private String parentKey;
    private boolean parent;
    private String icon;
    private String route;
    private Integer displayOrder;
    private boolean enabled;
    private PositionType positionType = PositionType.SPECIFIC;
    private String positionAfterItem; // Key of item to position after (used with AFTER_ITEM)
}