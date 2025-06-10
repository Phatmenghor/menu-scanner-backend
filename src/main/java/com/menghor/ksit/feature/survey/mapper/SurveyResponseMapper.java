package com.menghor.ksit.feature.survey.mapper;

import com.menghor.ksit.feature.auth.dto.resposne.UserBasicInfoDto;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.survey.dto.response.*;
import com.menghor.ksit.feature.survey.model.SurveyAnswerEntity;
import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SurveyResponseMapper {

    @Mapping(target = "surveyId", source = "survey.id")
    @Mapping(target = "user", source = "user", qualifiedByName = "mapUserBasicInfo")
    @Mapping(target = "answers", source = "answers", qualifiedByName = "mapAnswersSorted")
    StudentSurveyResponseDto toStudentResponseDto(SurveyResponseEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "surveyId", source = "survey.id")
    @Mapping(target = "surveyTitle", source = "survey.title")
    @Mapping(target = "student", source = "user", qualifiedByName = "mapUserBasicInfo")
    @Mapping(target = "schedule", source = "schedule", qualifiedByName = "mapScheduleBasicInfo")
    @Mapping(target = "answerDetails", source = "answers", qualifiedByName = "mapAnswerDetailsSorted")
    SurveyResponseDetailDto toDetailDto(SurveyResponseEntity entity);

    List<StudentSurveyResponseDto> toStudentResponseDtoList(List<SurveyResponseEntity> entities);

    default CustomPaginationResponseDto<StudentSurveyResponseDto> toPaginationResponse(Page<SurveyResponseEntity> page) {
        List<StudentSurveyResponseDto> content = toStudentResponseDtoList(page.getContent());

        return CustomPaginationResponseDto.<StudentSurveyResponseDto>builder()
                .content(content)
                .pageNo(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Named("mapUserBasicInfo")
    default UserBasicInfoDto mapUserBasicInfo(UserEntity user) {
        if (user == null) return null;

        UserBasicInfoDto dto = new UserBasicInfoDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEnglishFirstName(user.getEnglishFirstName());
        dto.setEnglishLastName(user.getEnglishLastName());
        dto.setKhmerFirstName(user.getKhmerFirstName());
        dto.setKhmerLastName(user.getKhmerLastName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIdentifyNumber(user.getIdentifyNumber());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setCurrentAddress(user.getCurrentAddress());
        dto.setProfileUrl(user.getProfileUrl());

        if (user.getClasses() != null && user.getClasses().getMajor() != null) {
            dto.setMajorName(user.getClasses().getMajor().getName());
            if (user.getClasses().getMajor().getDepartment() != null) {
                dto.setDepartmentName(user.getClasses().getMajor().getDepartment().getName());
            }
        }

        return dto;
    }

    @Named("mapScheduleBasicInfo")
    default ScheduleBasicInfoDto mapScheduleBasicInfo(com.menghor.ksit.feature.school.model.ScheduleEntity schedule) {
        if (schedule == null) return null;

        ScheduleBasicInfoDto dto = new ScheduleBasicInfoDto();
        dto.setId(schedule.getId());

        if (schedule.getCourse() != null) {
            dto.setCourseName(schedule.getCourse().getNameEn());
            dto.setCourseCode(schedule.getCourse().getCode());
        }

        if (schedule.getUser() != null) {
            dto.setTeacherName(getFormattedUserName(schedule.getUser()));
        }

        if (schedule.getClasses() != null) {
            dto.setClassName(schedule.getClasses().getCode());
        }

        if (schedule.getRoom() != null) {
            dto.setRoomName(schedule.getRoom().getName());
        }

        dto.setDayOfWeek(schedule.getDay());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());

        return dto;
    }

    @Named("mapAnswersSorted")
    default List<StudentSurveyAnswerDto> mapAnswersSorted(List<SurveyAnswerEntity> answers) {
        if (answers == null) return null;

        return answers.stream()
                .map(this::mapAnswer)
                .sorted(Comparator.comparing(StudentSurveyAnswerDto::getQuestionId))
                .collect(Collectors.toList());
    }

    @Named("mapAnswerDetailsSorted")
    default List<SurveyAnswerDetailDto> mapAnswerDetailsSorted(List<SurveyAnswerEntity> answers) {
        if (answers == null) return null;

        return answers.stream()
                .map(this::mapAnswerDetail)
                .sorted(Comparator.comparing(SurveyAnswerDetailDto::getDisplayOrder))
                .collect(Collectors.toList());
    }

    default StudentSurveyAnswerDto mapAnswer(SurveyAnswerEntity answer) {
        if (answer == null) return null;

        StudentSurveyAnswerDto dto = new StudentSurveyAnswerDto();
        dto.setId(answer.getId());
        dto.setQuestionId(answer.getQuestion().getId());
        dto.setQuestionText(answer.getQuestion().getQuestionText());
        dto.setTextAnswer(answer.getTextAnswer());
        dto.setRatingAnswer(answer.getRatingAnswer());

        return dto;
    }

    default SurveyAnswerDetailDto mapAnswerDetail(SurveyAnswerEntity answer) {
        if (answer == null) return null;

        SurveyAnswerDetailDto dto = new SurveyAnswerDetailDto();
        dto.setAnswerId(answer.getId());
        dto.setQuestionId(answer.getQuestion().getId());
        dto.setSectionTitle(answer.getQuestion().getSection().getTitle());
        dto.setQuestionText(answer.getQuestion().getQuestionText());
        dto.setQuestionType(answer.getQuestion().getQuestionType());
        dto.setTextAnswer(answer.getTextAnswer());
        dto.setRatingAnswer(answer.getRatingAnswer());
        dto.setMinRating(answer.getQuestion().getMinRating());
        dto.setMaxRating(answer.getQuestion().getMaxRating());
        dto.setLeftLabel(answer.getQuestion().getLeftLabel());
        dto.setRightLabel(answer.getQuestion().getRightLabel());
        dto.setDisplayOrder(answer.getQuestion().getDisplayOrder());
        dto.setSectionOrder(answer.getQuestion().getSection().getDisplayOrder());

        return dto;
    }

    default String getFormattedUserName(UserEntity user) {
        if (user.getEnglishFirstName() != null && user.getEnglishLastName() != null) {
            return user.getEnglishFirstName() + " " + user.getEnglishLastName();
        }
        if (user.getKhmerFirstName() != null && user.getKhmerLastName() != null) {
            return user.getKhmerFirstName() + " " + user.getKhmerLastName();
        }
        return user.getUsername();
    }
}