package com.menghor.ksit.feature.master.dto.filter;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class RoomFilterDto {
    private String search;
    private Status status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
