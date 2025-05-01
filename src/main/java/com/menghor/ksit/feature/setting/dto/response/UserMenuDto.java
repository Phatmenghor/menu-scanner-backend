package com.menghor.ksit.feature.setting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for user menu response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuDto {
    @Builder.Default
    private List<MenuItemDto> menuItems = new ArrayList<>();
}