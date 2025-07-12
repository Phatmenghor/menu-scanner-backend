package com.menghor.ksit.feature.menu.dto.response;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
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
    private String parentTitle;
    private List<MenuItemResponseDto> children;
    private LocalDateTime createdAt;
}