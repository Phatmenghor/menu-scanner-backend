package com.menghor.ksit.feature.school.dto.response;

import com.menghor.ksit.enumations.DayOfWeek;
import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class ScheduleResponseListDto {
    private Long id;
    private String startTime;
    private String endTime;
    private Integer academyYear;
    private DayOfWeek day;
    private Status status;
    private String className;
    private String teacherName;
    private String courseName;
    private String roomName;
    private String semesterName;
}