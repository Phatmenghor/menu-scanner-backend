// Fixed SurveyProgressServiceImpl.java with fallback methods
package com.menghor.ksit.feature.survey.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.StatusSurvey;
import com.menghor.ksit.enumations.SurveyStatus;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
import com.menghor.ksit.feature.survey.dto.response.ScheduleStudentSurveyDto;
import com.menghor.ksit.feature.survey.dto.response.ScheduleStudentsProgressDto;
import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import com.menghor.ksit.feature.survey.repository.SurveyResponseRepository;
import com.menghor.ksit.feature.survey.service.SurveyProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyProgressServiceImpl implements SurveyProgressService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final SurveyResponseRepository surveyResponseRepository;

    @Override
    public ScheduleStudentsProgressDto getScheduleStudentsProgress(Long scheduleId) {
        log.info("Getting students survey progress for schedule ID: {}", scheduleId);

        // Get schedule with necessary information
        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + scheduleId));

        // Get all students in the class for this schedule
        List<UserEntity> studentsInClass = getStudentsInClass(schedule.getClasses().getId());

        // Get survey responses for this schedule with fallback method
        Map<Long, SurveyResponseEntity> responseMap = getSurveyResponsesForSchedule(scheduleId);

        // Build response DTO
        ScheduleStudentsProgressDto progressDto = new ScheduleStudentsProgressDto();

        // Set schedule information
        setScheduleInfo(progressDto, schedule);

        // Convert students to DTOs with survey status
        List<ScheduleStudentSurveyDto> studentDtos = studentsInClass.stream()
                .map(student -> convertToStudentSurveyDto(student, responseMap))
                .sorted((s1, s2) -> {
                    // Sort by survey status (pending first) then by name
                    if (s1.getSurveyStatus() != s2.getSurveyStatus()) {
                        return s1.getSurveyStatus() == SurveyStatus.NOT_STARTED ? -1 : 1;
                    }
                    String name1 = s1.getEnglishFirstName() != null ? s1.getEnglishFirstName() : s1.getUsername();
                    String name2 = s2.getEnglishFirstName() != null ? s2.getEnglishFirstName() : s2.getUsername();
                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());

        progressDto.setStudents(studentDtos);

        // Calculate and set statistics
        calculateAndSetStatistics(progressDto, studentDtos);

        log.info("Students survey progress retrieved for schedule {}: {}/{} students completed",
                scheduleId, progressDto.getCompletedSurveys(), progressDto.getTotalStudents());

        return progressDto;
    }

    /**
     * Get all students enrolled in a specific class
     */
    private List<UserEntity> getStudentsInClass(Long classId) {
        return userRepository.findByClassesId(classId).stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == RoleEnum.STUDENT))
                .collect(Collectors.toList());
    }

    /**
     * Get survey responses for this schedule with fallback methods
     */
    private Map<Long, SurveyResponseEntity> getSurveyResponsesForSchedule(Long scheduleId) {
        try {
            // Try the optimized method first
            List<SurveyResponseEntity> responses = surveyResponseRepository.findCompletedResponsesByScheduleId(scheduleId);
            return responses.stream()
                    .collect(Collectors.toMap(
                            response -> response.getUser().getId(),
                            response -> response,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            log.warn("Optimized query failed, using fallback method: {}", e.getMessage());

            // Fallback method - get all responses and filter in service layer
            try {
                List<SurveyResponseEntity> allResponses = surveyResponseRepository.findByScheduleId(scheduleId);
                return allResponses.stream()
                        .filter(response -> response.getIsCompleted() != null && response.getIsCompleted())
                        .filter(response -> response.getStatus() == StatusSurvey.ACTIVE)
                        .collect(Collectors.toMap(
                                response -> response.getUser().getId(),
                                response -> response,
                                (existing, replacement) -> existing
                        ));
            } catch (Exception e2) {
                log.error("Both query methods failed, using manual approach: {}", e2.getMessage());

                // Final fallback - manual filtering
                return surveyResponseRepository.findAll().stream()
                        .filter(response -> response.getSchedule().getId().equals(scheduleId))
                        .filter(response -> response.getIsCompleted() != null && response.getIsCompleted())
                        .filter(response -> response.getStatus() == StatusSurvey.ACTIVE)
                        .collect(Collectors.toMap(
                                response -> response.getUser().getId(),
                                response -> response,
                                (existing, replacement) -> existing
                        ));
            }
        }
    }

    /**
     * Convert UserEntity to ScheduleStudentSurveyDto with survey status
     */
    private ScheduleStudentSurveyDto convertToStudentSurveyDto(UserEntity student, Map<Long, SurveyResponseEntity> responseMap) {
        ScheduleStudentSurveyDto dto = new ScheduleStudentSurveyDto();

        // Copy all fields from StudentUserListResponseDto equivalent
        dto.setId(student.getId());
        dto.setUsername(student.getUsername());
        dto.setEmail(student.getEmail());
        dto.setStatus(student.getStatus());
        dto.setKhmerFirstName(student.getKhmerFirstName());
        dto.setKhmerLastName(student.getKhmerLastName());
        dto.setEnglishFirstName(student.getEnglishFirstName());
        dto.setEnglishLastName(student.getEnglishLastName());
        dto.setProfileUrl(student.getProfileUrl());
        dto.setGender(student.getGender());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setIdentifyNumber(student.getIdentifyNumber());
        dto.setCreatedAt(student.getCreatedAt());

        // Set student class
        if (student.getClasses() != null) {
            dto.setStudentClass(convertToClassResponse(student.getClasses()));
        }

        // Set survey status
        SurveyResponseEntity response = responseMap.get(student.getId());
        if (response != null) {
            dto.setSurveyStatus(SurveyStatus.COMPLETED);
            dto.setSurveySubmittedAt(response.getSubmittedAt());
            dto.setSurveyResponseId(response.getId());
        } else {
            dto.setSurveyStatus(SurveyStatus.NOT_STARTED);
            dto.setSurveySubmittedAt(null);
            dto.setSurveyResponseId(null);
        }

        return dto;
    }

    /**
     * Convert ClassEntity to ClassResponseDto (simplified)
     */
    private ClassResponseDto convertToClassResponse(com.menghor.ksit.feature.master.model.ClassEntity classEntity) {
        ClassResponseDto classDto = new ClassResponseDto();
        classDto.setId(classEntity.getId());
        classDto.setCode(classEntity.getCode());
        classDto.setAcademyYear(classEntity.getAcademyYear());
        classDto.setCreatedAt(classEntity.getCreatedAt());
        // Add other fields if needed
        return classDto;
    }

    /**
     * Set schedule information in the progress DTO
     */
    private void setScheduleInfo(ScheduleStudentsProgressDto progressDto, ScheduleEntity schedule) {
        progressDto.setScheduleId(schedule.getId());

        if (schedule.getCourse() != null) {
            progressDto.setCourseName(schedule.getCourse().getNameEn());
            progressDto.setCourseCode(schedule.getCourse().getCode());
        }

        if (schedule.getClasses() != null) {
            progressDto.setClassName(schedule.getClasses().getCode());
        }

        if (schedule.getUser() != null) {
            progressDto.setTeacherName(getFormattedUserName(schedule.getUser()));
        }

        if (schedule.getRoom() != null) {
            progressDto.setRoomName(schedule.getRoom().getName());
        }

        if (schedule.getDay() != null) {
            progressDto.setDayOfWeek(schedule.getDay().name());
        }

        // Set separate time fields instead of combined timeSlot
        if (schedule.getStartTime() != null) {
            progressDto.setStartTime(schedule.getStartTime());
        }

        if (schedule.getEndTime() != null) {
            progressDto.setEndTime(schedule.getEndTime());
        }

        // Set separate semester fields instead of combined semesterDisplay
        if (schedule.getSemester() != null) {
            progressDto.setSemester(schedule.getSemester().getSemester());
            progressDto.setAcademyYear(schedule.getSemester().getAcademyYear());
        }
    }

    /**
     * Calculate and set statistics
     */
    private void calculateAndSetStatistics(ScheduleStudentsProgressDto progressDto, List<ScheduleStudentSurveyDto> students) {
        int totalStudents = students.size();
        int completedSurveys = (int) students.stream()
                .filter(s -> s.getSurveyStatus() == SurveyStatus.COMPLETED)
                .count();
        int pendingSurveys = totalStudents - completedSurveys;

        progressDto.setTotalStudents(totalStudents);
        progressDto.setCompletedSurveys(completedSurveys);
        progressDto.setPendingSurveys(pendingSurveys);

        if (totalStudents > 0) {
            double percentage = (double) completedSurveys / totalStudents * 100;
            progressDto.setCompletionPercentage(
                    BigDecimal.valueOf(percentage)
                            .setScale(1, RoundingMode.HALF_UP)
                            .doubleValue()
            );
        } else {
            progressDto.setCompletionPercentage(0.0);
        }
    }

    /**
     * Get formatted user name
     */
    private String getFormattedUserName(UserEntity user) {
        if (user.getEnglishFirstName() != null && user.getEnglishLastName() != null) {
            return user.getEnglishFirstName() + " " + user.getEnglishLastName();
        }
        if (user.getKhmerFirstName() != null && user.getKhmerLastName() != null) {
            return user.getKhmerFirstName() + " " + user.getKhmerLastName();
        }
        return user.getUsername();
    }
}