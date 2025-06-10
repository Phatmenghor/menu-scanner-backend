package com.menghor.ksit.feature.survey.dto.response;

import com.menghor.ksit.enumations.DayOfWeek;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ScheduleBasicInfoDto {
    private Long id;
    private String courseName;
    private String courseCode;
    private String teacherName;
    private String className;
    private String roomName;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeSlot;
}