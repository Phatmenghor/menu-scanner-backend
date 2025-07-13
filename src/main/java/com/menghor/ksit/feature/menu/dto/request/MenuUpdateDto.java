package com.menghor.ksit.feature.menu.dto.request;

import lombok.Data;

@Data
public class MenuUpdateDto {
    private String title;
    private String route;
    private String icon;
    private Boolean isParent;
    private Long parentId;
    private Integer displayOrder;
}