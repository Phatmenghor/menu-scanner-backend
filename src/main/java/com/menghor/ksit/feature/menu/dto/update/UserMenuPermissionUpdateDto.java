package com.menghor.ksit.feature.menu.dto.update;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class UserMenuPermissionUpdateDto {
    private Boolean canView;
    private Integer displayOrder;
    private String customTitle;
    private String customIcon;
    private Status status;
}