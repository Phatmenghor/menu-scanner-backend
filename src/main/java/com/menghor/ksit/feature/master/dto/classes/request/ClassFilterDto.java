package com.menghor.ksit.feature.master.dto.classes.request;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class ClassFilterDto {
    private String search;
    private Integer academyYear;
    private Status status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
