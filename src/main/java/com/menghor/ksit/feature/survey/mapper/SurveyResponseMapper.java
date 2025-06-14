package com.menghor.ksit.feature.survey.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.menghor.ksit.enumations.QuestionTypeEnum;
import com.menghor.ksit.feature.auth.dto.resposne.UserBasicInfoDto;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.survey.dto.response.*;
import com.menghor.ksit.feature.survey.model.SurveyAnswerEntity;
import com.menghor.ksit.feature.survey.model.SurveyEntity;
import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class SurveyResponseMapper {

    @Autowired
    private ObjectMapper objectMapper;

    @Mapping(target = "surveyId", source = "survey.id")
    @Mapping(target = "surveyTitle", source = "surveyTitleSnapshot")
    @Mapping(target = "surveyDescription", source = "surveyDescriptionSnapshot")
    @Mapping(target = "user", source = "user", qualifiedByName = "mapUserBasicInfo")
    @Mapping(target = "sections", source = ".", qualifiedByName = "mapSurveySnapshot")
    public abstract StudentSurveyResponseDto toStudentResponseDto(SurveyResponseEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "surveyId", source = "survey.id")
    @Mapping(target = "surveyTitle", source = "surveyTitleSnapshot")
    @Mapping(target = "surveyDescription", source = "surveyDescriptionSnapshot")
    @Mapping(target = "student", source = "user", qualifiedByName = "mapUserBasicInfo")
    @Mapping(target = "schedule", source = "schedule", qualifiedByName = "mapScheduleBasicInfo")
    @Mapping(target = "sections", source = ".", qualifiedByName = "mapSurveySnapshot")
    public abstract SurveyResponseDetailDto toDetailDto(SurveyResponseEntity entity);

    public abstract List<StudentSurveyResponseDto> toStudentResponseDtoList(List<SurveyResponseEntity> entities);

    public CustomPaginationResponseDto<StudentSurveyResponseDto> toPaginationResponse(Page<SurveyResponseEntity> page) {
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
    public UserBasicInfoDto mapUserBasicInfo(UserEntity user) {
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
    public ScheduleBasicInfoDto mapScheduleBasicInfo(com.menghor.ksit.feature.school.model.ScheduleEntity schedule) {
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

    @Named("mapSurveySnapshot")
    public List<SurveyResponseSectionDto> mapSurveySnapshot(SurveyResponseEntity entity) {
        if (entity.getSurveySnapshot() == null || entity.getSurveySnapshot().isEmpty()) {
            log.warn("No survey snapshot found for response ID: {}", entity.getId());
            return new ArrayList<>();
        }

        try {
            // Parse the stored survey snapshot
            SurveySnapshotDto snapshot = objectMapper.readValue(entity.getSurveySnapshot(), SurveySnapshotDto.class);

            // Create answer map for quick lookup
            Map<Long, SurveyAnswerEntity> answerMap = entity.getAnswers().stream()
                    .collect(Collectors.toMap(
                            answer -> answer.getQuestion().getId(),
                            answer -> answer,
                            (existing, replacement) -> existing
                    ));

            // Map sections with answers
            return snapshot.getSections().stream()
                    .map(sectionSnapshot -> {
                        SurveyResponseSectionDto sectionDto = new SurveyResponseSectionDto();
                        sectionDto.setSectionId(sectionSnapshot.getId());
                        sectionDto.setTitle(sectionSnapshot.getTitle());
                        sectionDto.setDescription(sectionSnapshot.getDescription());
                        sectionDto.setDisplayOrder(sectionSnapshot.getDisplayOrder());

                        // Map questions with answers
                        List<SurveyResponseQuestionDto> questions = sectionSnapshot.getQuestions().stream()
                                .map(questionSnapshot -> {
                                    SurveyResponseQuestionDto questionDto = new SurveyResponseQuestionDto();
                                    questionDto.setQuestionId(questionSnapshot.getId());
                                    questionDto.setQuestionText(questionSnapshot.getQuestionText());
                                    questionDto.setQuestionType(questionSnapshot.getQuestionType());
                                    questionDto.setRequired(questionSnapshot.getRequired());
                                    questionDto.setDisplayOrder(questionSnapshot.getDisplayOrder());
                                    questionDto.setMinRating(questionSnapshot.getMinRating());
                                    questionDto.setMaxRating(questionSnapshot.getMaxRating());
                                    questionDto.setLeftLabel(questionSnapshot.getLeftLabel());
                                    questionDto.setRightLabel(questionSnapshot.getRightLabel());

                                    // Generate rating options if it's a rating question
                                    if (questionSnapshot.getQuestionType() == QuestionTypeEnum.RATING) {
                                        questionDto.setRatingOptions(generateRatingOptions(questionSnapshot));
                                    }

                                    // Add student's answer if exists
                                    SurveyAnswerEntity answer = answerMap.get(questionSnapshot.getId());
                                    if (answer != null) {
                                        questionDto.setAnswerId(answer.getId());
                                        questionDto.setTextAnswer(answer.getTextAnswer());
                                        questionDto.setRatingAnswer(answer.getRatingAnswer());
                                    }

                                    return questionDto;
                                })
                                .collect(Collectors.toList());

                        sectionDto.setQuestions(questions);
                        return sectionDto;
                    })
                    .collect(Collectors.toList());

        } catch (JsonProcessingException e) {
            log.error("Error parsing survey snapshot for response ID: {}", entity.getId(), e);
            return new ArrayList<>();
        }
    }

    private List<RatingOptionDto> generateRatingOptions(SurveyQuestionSnapshotDto question) {
        List<RatingOptionDto> options = new ArrayList<>();
        int minRating = question.getMinRating() != null ? question.getMinRating() : 1;
        int maxRating = question.getMaxRating() != null ? question.getMaxRating() : 5;

        for (int i = minRating; i <= maxRating; i++) {
            // Simple rating labels - just numbers
            options.add(new RatingOptionDto(i, String.valueOf(i)));
        }
        return options;
    }

    public String getFormattedUserName(UserEntity user) {
        if (user.getEnglishFirstName() != null && user.getEnglishLastName() != null) {
            return user.getEnglishFirstName() + " " + user.getEnglishLastName();
        }
        if (user.getKhmerFirstName() != null && user.getKhmerLastName() != null) {
            return user.getKhmerFirstName() + " " + user.getKhmerLastName();
        }
        return user.getUsername();
    }

    // Helper method to create survey snapshot JSON from survey entity
    public String createSurveySnapshot(SurveyEntity survey) {
        try {
            SurveySnapshotDto snapshot = new SurveySnapshotDto();
            snapshot.setId(survey.getId());
            snapshot.setTitle(survey.getTitle());
            snapshot.setDescription(survey.getDescription());

            List<SurveySectionSnapshotDto> sections = survey.getActiveSections().stream()
                    .map(section -> {
                        SurveySectionSnapshotDto sectionSnapshot = new SurveySectionSnapshotDto();
                        sectionSnapshot.setId(section.getId());
                        sectionSnapshot.setTitle(section.getTitle());
                        sectionSnapshot.setDescription(section.getDescription());
                        sectionSnapshot.setDisplayOrder(section.getDisplayOrder());

                        List<SurveyQuestionSnapshotDto> questions = section.getActiveQuestions().stream()
                                .map(question -> {
                                    SurveyQuestionSnapshotDto questionSnapshot = new SurveyQuestionSnapshotDto();
                                    questionSnapshot.setId(question.getId());
                                    questionSnapshot.setQuestionText(question.getQuestionText());
                                    questionSnapshot.setQuestionType(question.getQuestionType());
                                    questionSnapshot.setRequired(question.getRequired());
                                    questionSnapshot.setDisplayOrder(question.getDisplayOrder());
                                    questionSnapshot.setMinRating(question.getMinRating());
                                    questionSnapshot.setMaxRating(question.getMaxRating());
                                    questionSnapshot.setLeftLabel(question.getLeftLabel());
                                    questionSnapshot.setRightLabel(question.getRightLabel());
                                    return questionSnapshot;
                                })
                                .sorted(Comparator.comparing(SurveyQuestionSnapshotDto::getDisplayOrder))
                                .collect(Collectors.toList());

                        sectionSnapshot.setQuestions(questions);
                        return sectionSnapshot;
                    })
                    .sorted(Comparator.comparing(SurveySectionSnapshotDto::getDisplayOrder))
                    .collect(Collectors.toList());

            snapshot.setSections(sections);
            return objectMapper.writeValueAsString(snapshot);

        } catch (JsonProcessingException e) {
            log.error("Error creating survey snapshot", e);
            throw new RuntimeException("Error creating survey snapshot", e);
        }
    }
}