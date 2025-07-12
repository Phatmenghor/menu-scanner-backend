package com.menghor.ksit.feature.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMenuResponseDto {
    private Long id;
    private String code;
    private String title;
    private String route;
    private String icon;
    private Integer displayOrder;
    private Boolean canView;
    private Boolean isParent;
    private Boolean isCustomized; // True if user has custom permissions
    private String customTitle;
    private String customIcon;
    private List<UserMenuResponseDto> children = new ArrayList<>();
}