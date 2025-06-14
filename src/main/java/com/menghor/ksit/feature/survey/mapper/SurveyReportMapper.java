package com.menghor.ksit.feature.survey.mapper;

import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.survey.dto.response.SurveyReportHeaderDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyReportRowDto;
import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface SurveyReportMapper {

    // Map response entity to report row
    @Mapping(target = "responseId", source = "id")
    @Mapping(target = "studentId", source = "user.id")
    @Mapping(target = "identifyNumber", source = "user.identifyNumber")
    @Mapping(target = "studentNameEnglish", source = "user", qualifiedByName = "mapEnglishName")
    @Mapping(target = "studentNameKhmer", source = "user", qualifiedByName = "mapKhmerName")
    @Mapping(target = "studentEmail", source = "user.email")
    @Mapping(target = "studentPhone", source = "user.phoneNumber")
    @Mapping(target = "className", source = "user.classes.code")
    @Mapping(target = "majorName", source = "user.classes.major.name")
    @Mapping(target = "departmentName", source = "user.classes.major.department.name")
    @Mapping(target = "scheduleId", source = "schedule.id")
    @Mapping(target = "courseCode", source = "schedule.course.code")
    @Mapping(target = "courseName", source = "schedule.course.nameEn")
    @Mapping(target = "teacherName", source = "schedule.user", qualifiedByName = "mapEnglishName")
    @Mapping(target = "roomName", source = "schedule.room.name")
    @Mapping(target = "dayOfWeek", source = "schedule.day")
    @Mapping(target = "timeSlot", source = "schedule", qualifiedByName = "mapTimeSlot")
    @Mapping(target = "semester", source = "schedule.semester.semester")
    @Mapping(target = "academyYear", source = "schedule.semester.academyYear")
    @Mapping(target = "surveyTitle", source = "surveyTitleSnapshot")
    @Mapping(target = "submittedAt", source = "submittedAt")
    @Mapping(target = "overallComment", source = "overallComment")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "dynamicAnswers", source = ".", qualifiedByName = "mapAnswersToMap")
    SurveyReportRowDto toReportRow(SurveyResponseEntity entity);

    List<SurveyReportRowDto> toReportRowList(List<SurveyResponseEntity> entities);

    // Create CustomPaginationResponseDto
    default CustomPaginationResponseDto<SurveyReportRowDto> toPaginationResponse(Page<SurveyResponseEntity> page) {
        List<SurveyReportRowDto> content = toReportRowList(page.getContent());
        
        return CustomPaginationResponseDto.<SurveyReportRowDto>builder()
                .content(content)
                .pageNo(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // Helper method to create header
    default SurveyReportHeaderDto createHeader(String key, String label, String type, String category, Long questionId, int order) {
        SurveyReportHeaderDto header = new SurveyReportHeaderDto();
        header.setKey(key);
        header.setLabel(label);
        header.setType(type);
        header.setCategory(category);
        header.setQuestionId(questionId);
        header.setDisplayOrder(order);
        return header;
    }

    // Named mapping methods
    @Named("mapEnglishName")
    default String mapEnglishName(UserEntity user) {
        if (user == null) return null;
        if (user.getEnglishFirstName() != null && user.getEnglishLastName() != null) {
            return user.getEnglishFirstName() + " " + user.getEnglishLastName();
        }
        return user.getUsername();
    }

    @Named("mapKhmerName")
    default String mapKhmerName(UserEntity user) {
        if (user == null) return null;
        if (user.getKhmerFirstName() != null && user.getKhmerLastName() != null) {
            return user.getKhmerFirstName() + " " + user.getKhmerLastName();
        }
        return null;
    }

    @Named("mapTimeSlot")
    default String mapTimeSlot(ScheduleEntity schedule) {
        if (schedule == null || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return null;
        }
        return schedule.getStartTime() + " - " + schedule.getEndTime();
    }

    @Named("mapAnswersToMap")
    default Map<String, Object> mapAnswersToMap(SurveyResponseEntity response) {
        Map<String, Object> answersMap = new HashMap<>();
        if (response.getAnswers() != null) {
            response.getAnswers().forEach(answer -> {
                String questionKey = "Q" + answer.getQuestion().getId() + "_Answer";
                Object answerValue = answer.getRatingAnswer() != null ? 
                    answer.getRatingAnswer() : answer.getTextAnswer();
                answersMap.put(questionKey, answerValue);
            });
        }
        return answersMap;
    }
}