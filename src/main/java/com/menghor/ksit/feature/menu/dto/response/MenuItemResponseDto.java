package com.menghor.ksit.feature.menu.dto.response;

import com.menghor.ksit.enumations.Status;
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
public class MenuItemResponseDto {
    private Long id;
    private String code;
    private String title;
    private String route;
    private String icon;
    private Integer displayOrder;
    private Status status;
    private Boolean isParent;
    private Long parentId;
    private List<MenuItemResponseDto> children = new ArrayList<>();
}
