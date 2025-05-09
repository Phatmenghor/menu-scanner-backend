package com.menghor.ksit.feature.master.dto.update;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class DepartmentUpdateDto {
    private String code;
    private String name;
    private String urlLogo;
    private Status status = Status.ACTIVE;
}
