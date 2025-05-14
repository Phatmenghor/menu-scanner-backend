package com.menghor.ksit.feature.school.dto.filter;

import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.SemesterEnum;
import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class ScheduleFilterDto {
    private String search;
    private Long classId;
    private Long roomId;
    private Long teacherId;
    private Integer academyYear;
    private SemesterEnum semester;
    private DayOfWeek dayOfWeek;
    private Status status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}