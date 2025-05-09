package com.menghor.ksit.feature.master.dto.response;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class MajorResponseListDto {
    private Long id;
    private String code;
    private String name;
    private String departmentName;
    private Status status;
}
