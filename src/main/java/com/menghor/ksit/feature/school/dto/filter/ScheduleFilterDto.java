package com.menghor.ksit.feature.school.dto.filter;

import com.menghor.ksit.enumations.SemesterEnum;
import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class ScheduleFilterDto {
    private String search;
    private Long classId;
    private Long roomId;
    private Long teacherId;
    private Integer academyYear; // For filtering by semester's academyYear
    private SemesterEnum semester; // For filtering by semester type
    private Status status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}