package com.menghor.ksit.feature.master.dto.update;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class MajorUpdateDto {
    private String code;
    private String name;
    private Long departmentId;
    private Status status = Status.ACTIVE;
}