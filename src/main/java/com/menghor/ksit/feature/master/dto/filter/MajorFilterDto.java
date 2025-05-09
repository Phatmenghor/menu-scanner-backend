package com.menghor.ksit.feature.master.dto.filter;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class MajorFilterDto {
    private String search;
    private Status status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
