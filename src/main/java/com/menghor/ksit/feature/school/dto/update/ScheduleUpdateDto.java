package com.menghor.ksit.feature.school.dto.update;

import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class ScheduleUpdateDto {
    private String startTime;
    private String endTime;
    private Integer academyYear;
    private DayOfWeek day;
    private Long classId;
    private Long teacherId;
    private Long courseId;
    private Long roomId;
    private Long semesterId;
    private Status status;
}