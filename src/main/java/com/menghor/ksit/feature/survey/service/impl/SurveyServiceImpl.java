package com.menghor.ksit.feature.survey.service.impl;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.SurveyStatus;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.*;
import com.menghor.ksit.feature.survey.dto.update.SurveyUpdateDto;
import com.menghor.ksit.feature.survey.mapper.SurveyMapper;
import com.menghor.ksit.feature.survey.mapper.SurveyResponseMapper;
import com.menghor.ksit.feature.survey.model.*;
import com.menghor.ksit.feature.survey.repository.*;
import com.menghor.ksit.feature.survey.service.SurveyService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.database.SecurityUtils;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final SurveySectionRepository surveySectionRepository;
    private final ScheduleRepository scheduleRepository;
    private final SurveyMapper surveyMapper;
    private final SurveyResponseMapper responseMapper;
    private final SecurityUtils securityUtils;

    @Override
    public SurveyResponseDto getMainSurvey() {
        log.info("Fetching main survey for admin view");

        SurveyEntity mainSurvey = getMainSurveyEntity();
        SurveyResponseDto responseDto = surveyMapper.toResponseDto(mainSurvey);

        // Add total responses count (across all schedules)
        responseDto.setTotalResponses(surveyRepository.countResponsesBySurveyId(mainSurvey.getId()));
        responseDto.setHasUserResponded(false); // Not applicable for admin view

        return responseDto;
    }

    @Override
    public SurveyResponseDto getSurveyForSchedule(Long scheduleId) {
        log.info("Fetching survey for schedule ID: {}", scheduleId);

        // Verify schedule exists and user has access
        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + scheduleId));

        UserEntity currentUser = securityUtils.getCurrentUser();

        // Verify student is enrolled in this schedule
        if (!isStudentEnrolledInSchedule(currentUser.getId(), scheduleId)) {
            log.info("User {} is not enrolled in schedule {}", currentUser.getId(), scheduleId);
//            throw new BadRequestException("You are not enrolled in this schedule");
        }

        SurveyEntity mainSurvey = getMainSurveyEntity();
        SurveyResponseDto responseDto = surveyMapper.toResponseDto(mainSurvey);

        // Add schedule-specific data
        responseDto.setTotalResponses(surveyRepository.countResponsesBySurveyIdAndScheduleId(mainSurvey.getId(), scheduleId));
        responseDto.setHasUserResponded(
                surveyRepository.hasUserRespondedForSchedule(mainSurvey.getId(), currentUser.getId(), scheduleId)
        );

        return responseDto;
    }

    @Override
    @Transactional
    public SurveyResponseDto updateMainSurvey(SurveyUpdateDto updateDto) {
        log.info("Updating main survey with title: {}", updateDto.getTitle());

        UserEntity currentUser = securityUtils.getCurrentUser();

        // Check if user has admin/staff privileges
        boolean hasAdminRole = currentUser.getRoles().stream()
                .map(Role::getName)
                .anyMatch(role -> role == RoleEnum.ADMIN || role == RoleEnum.DEVELOPER || role == RoleEnum.STAFF);

        if (!hasAdminRole) {
            throw new BadRequestException("Only admin/staff can update surveys");
        }

        SurveyEntity mainSurvey = getMainSurveyEntity();

        // Update basic fields
        mainSurvey.setTitle(updateDto.getTitle());
        mainSurvey.setDescription(updateDto.getDescription());

        // Clear existing sections and questions (cascade will handle deletion)
        mainSurvey.getSections().clear();
        surveyRepository.save(mainSurvey);

        // Add new sections and questions
        if (updateDto.getSections() != null) {
            for (int i = 0; i < updateDto.getSections().size(); i++) {
                var sectionDto = updateDto.getSections().get(i);

                SurveySectionEntity section = new SurveySectionEntity();
                section.setTitle(sectionDto.getTitle());
                section.setDescription(sectionDto.getDescription());
                section.setDisplayOrder(sectionDto.getDisplayOrder() != null ? sectionDto.getDisplayOrder() : i);
                section.setSurvey(mainSurvey);

                mainSurvey.getSections().add(section);

                // Create questions for this section
                if (sectionDto.getQuestions() != null) {
                    for (int j = 0; j < sectionDto.getQuestions().size(); j++) {
                        var questionDto = sectionDto.getQuestions().get(j);

                        SurveyQuestionEntity question = new SurveyQuestionEntity();
                        question.setQuestionText(questionDto.getQuestionText());
                        question.setQuestionType(questionDto.getQuestionType());
                        question.setRequired(questionDto.getRequired());
                        question.setDisplayOrder(questionDto.getDisplayOrder() != null ? questionDto.getDisplayOrder() : j);
                        question.setMinRating(questionDto.getMinRating());
                        question.setMaxRating(questionDto.getMaxRating());
                        question.setLeftLabel(questionDto.getLeftLabel());
                        question.setRightLabel(questionDto.getRightLabel());
                        question.setSection(section);

                        section.getQuestions().add(question);
                    }
                }
            }
        }

        SurveyEntity savedSurvey = surveyRepository.save(mainSurvey);
        log.info("Main survey updated successfully with ID: {}", savedSurvey.getId());

        return surveyMapper.toResponseDto(savedSurvey);
    }

    @Override
    @Transactional
    public StudentSurveyResponseDto submitSurveyResponseForSchedule(Long scheduleId, SurveyResponseSubmitDto submitDto) {
        log.info("Submitting survey response for schedule ID: {}", scheduleId);

        UserEntity currentUser = securityUtils.getCurrentUser();
        SurveyEntity mainSurvey = getMainSurveyEntity();

        // Verify schedule exists
        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + scheduleId));

        // Check if user already responded for this schedule
        if (surveyRepository.hasUserRespondedForSchedule(mainSurvey.getId(), currentUser.getId(), scheduleId)) {
            throw new BadRequestException("You have already responded to the survey for this schedule");
        }

        // Verify student is enrolled in this schedule
        if (!isStudentEnrolledInSchedule(currentUser.getId(), scheduleId)) {
            throw new BadRequestException("You are not enrolled in this schedule");
        }

        // Create survey response
        SurveyResponseEntity response = new SurveyResponseEntity();
        response.setSurvey(mainSurvey);
        response.setUser(currentUser);
        response.setSchedule(schedule);
        response.setSubmittedAt(LocalDateTime.now());
        response.setIsCompleted(true);
        response.setOverallComment(submitDto.getOverallComment());
        response.setOverallRating(submitDto.getOverallRating());

        SurveyResponseEntity savedResponse = surveyResponseRepository.save(response);

        // Create individual answers
        if (submitDto.getAnswers() != null) {
            for (var answerDto : submitDto.getAnswers()) {
                SurveyQuestionEntity question = surveyQuestionRepository.findById(answerDto.getQuestionId())
                        .orElseThrow(() -> new NotFoundException("Question not found with ID: " + answerDto.getQuestionId()));

                SurveyAnswerEntity answer = new SurveyAnswerEntity();
                answer.setResponse(savedResponse);
                answer.setQuestion(question);
                answer.setTextAnswer(answerDto.getTextAnswer());
                answer.setRatingAnswer(answerDto.getRatingAnswer());

                surveyAnswerRepository.save(answer);
                savedResponse.getAnswers().add(answer);
            }
        }

        log.info("Survey response submitted successfully with ID: {} for schedule: {}", savedResponse.getId(), scheduleId);
        return responseMapper.toStudentResponseDto(savedResponse);
    }

    @Override
    public StudentSurveyResponseDto getMyResponseForSchedule(Long scheduleId) {
        log.info("Fetching current user's survey response for schedule ID: {}", scheduleId);

        UserEntity currentUser = securityUtils.getCurrentUser();

        Optional<SurveyResponseEntity> responseOpt = surveyResponseRepository
                .findByUserIdAndScheduleId(currentUser.getId(), scheduleId);

        if (responseOpt.isEmpty()) {
            throw new NotFoundException("You have not responded to the survey for this schedule yet");
        }

        return responseMapper.toStudentResponseDto(responseOpt.get());
    }

    @Override
    public SurveyStatus getSurveyStatusForSchedule(Long scheduleId) {
        log.info("Getting survey status for schedule ID: {}", scheduleId);

        UserEntity currentUser = securityUtils.getCurrentUser();
        SurveyEntity mainSurvey = getMainSurveyEntity();

        boolean hasResponded = surveyRepository.hasUserRespondedForSchedule(
                mainSurvey.getId(), currentUser.getId(), scheduleId);

        return hasResponded ? SurveyStatus.COMPLETED : SurveyStatus.NOT_STARTED;
    }

    @Override
    public CustomPaginationResponseDto<StudentSurveyResponseDto> getScheduleSurveyResponses(Long scheduleId, int pageNo, int pageSize) {
        log.info("Fetching survey responses for schedule ID: {} - page: {}, size: {}", scheduleId, pageNo, pageSize);

        Pageable pageable = PaginationUtils.createPageable(pageNo, pageSize, "submittedAt", "DESC");
        Page<SurveyResponseEntity> responsePage = surveyResponseRepository.findByScheduleId(scheduleId, pageable);

        return responseMapper.toPaginationResponse(responsePage);
    }

    @Override
    public SurveyResponseDetailDto getStudentResponseDetail(Long responseId) {
        log.info("Fetching survey response detail for ID: {}", responseId);

        SurveyResponseEntity response = surveyResponseRepository.findById(responseId)
                .orElseThrow(() -> new NotFoundException("Survey response not found with ID: " + responseId));

        // Build detailed response DTO
        SurveyResponseDetailDto detailDto = new SurveyResponseDetailDto();
        detailDto.setId(response.getId());
        detailDto.setSurveyId(response.getSurvey().getId());
        detailDto.setSurveyTitle(response.getSurvey().getTitle());
        detailDto.setSubmittedAt(response.getSubmittedAt());
        detailDto.setIsCompleted(response.getIsCompleted());
        detailDto.setCreatedAt(response.getCreatedAt());

        // Set answer details
        var answerDetails = response.getAnswers().stream()
                .map(answer -> {
                    SurveyAnswerDetailDto answerDetailDto = new SurveyAnswerDetailDto();
                    answerDetailDto.setAnswerId(answer.getId());
                    answerDetailDto.setQuestionId(answer.getQuestion().getId());
                    answerDetailDto.setSectionTitle(answer.getQuestion().getSection().getTitle());
                    answerDetailDto.setQuestionText(answer.getQuestion().getQuestionText());
                    answerDetailDto.setQuestionType(answer.getQuestion().getQuestionType());
                    answerDetailDto.setTextAnswer(answer.getTextAnswer());
                    answerDetailDto.setRatingAnswer(answer.getRatingAnswer());
                    answerDetailDto.setMinRating(answer.getQuestion().getMinRating());
                    answerDetailDto.setMaxRating(answer.getQuestion().getMaxRating());
                    answerDetailDto.setLeftLabel(answer.getQuestion().getLeftLabel());
                    answerDetailDto.setRightLabel(answer.getQuestion().getRightLabel());
                    answerDetailDto.setDisplayOrder(answer.getQuestion().getDisplayOrder());
                    return answerDetailDto;
                })
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .toList();

        detailDto.setAnswerDetails(answerDetails);

        return detailDto;
    }

    @Override
    public SurveyStatisticsDto getSurveyStatistics(Long scheduleId) {
        log.info("Getting survey statistics for schedule ID: {}", scheduleId);

        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + scheduleId));

        SurveyEntity mainSurvey = getMainSurveyEntity();

        // Get total students in the schedule
        Integer totalStudents = scheduleRepository.countStudentsByScheduleId(scheduleId);

        // Get completed responses
        Integer completedResponses = surveyRepository.countResponsesBySurveyIdAndScheduleId(
                mainSurvey.getId(), scheduleId);

        // Calculate completion rate
        Double completionRate = totalStudents > 0 ?
                (completedResponses.doubleValue() / totalStudents.doubleValue()) * 100 : 0.0;

        // Get average rating
        Double averageRating = surveyRepository.getAverageRatingForSchedule(mainSurvey.getId(), scheduleId);

        // Get total questions count
        Integer totalQuestions = mainSurvey.getSections().stream()
                .mapToInt(section -> section.getQuestions().size())
                .sum();

        SurveyStatisticsDto statistics = new SurveyStatisticsDto();
        statistics.setScheduleId(scheduleId);
        statistics.setScheduleName(schedule.getCourse().getCode() + " - " + schedule.getClasses().getCode());
        statistics.setTotalStudents(totalStudents);
        statistics.setCompletedResponses(completedResponses);
        statistics.setPendingResponses(totalStudents - completedResponses);
        statistics.setCompletionRate(completionRate);
        statistics.setAverageRating(averageRating);
        statistics.setTotalQuestions(totalQuestions);

        return statistics;
    }

    @Override
    @Transactional
    public void initializeMainSurvey() {
        log.info("Initializing main survey if not exists");

        Optional<SurveyEntity> existingSurvey = surveyRepository.findByStatus(Status.ACTIVE);
        if (existingSurvey.isPresent()) {
            log.info("Main survey already exists with ID: {}", existingSurvey.get().getId());
            return;
        }

        // Create default survey
        SurveyEntity mainSurvey = new SurveyEntity();
        mainSurvey.setTitle("Student Course Evaluation Survey");
        mainSurvey.setDescription("Please provide your feedback about your learning experience in this course");
        mainSurvey.setStatus(Status.ACTIVE);

        // Set creator
        try {
            UserEntity currentUser = securityUtils.getCurrentUser();
            mainSurvey.setCreatedBy(currentUser);
        } catch (Exception e) {
            log.warn("Could not set current user as survey creator: {}", e.getMessage());
        }

        // Create default sections and questions
        createDefaultSurveyContent(mainSurvey);

        SurveyEntity savedSurvey = surveyRepository.save(mainSurvey);
        log.info("Main survey created successfully with ID: {}", savedSurvey.getId());
    }

    @Override
    public Boolean hasUserCompletedSurvey(Long userId, Long scheduleId) {
        SurveyEntity mainSurvey = getMainSurveyEntity();
        return surveyRepository.hasUserRespondedForSchedule(mainSurvey.getId(), userId, scheduleId);
    }

    // Private helper methods

    private SurveyEntity getMainSurveyEntity() {
        return surveyRepository.findByStatus(Status.ACTIVE)
                .orElseThrow(() -> {
                    log.error("Main survey not found. Please initialize the survey first.");
                    return new NotFoundException("Main survey not found. Please contact administrator.");
                });
    }

    private boolean isStudentEnrolledInSchedule(Long studentId, Long scheduleId) {
        return scheduleRepository.existsByIdAndClassesStudentsId(scheduleId, studentId);
    }

    private void createDefaultSurveyContent(SurveyEntity survey) {
        // Create a default section
        SurveySectionEntity defaultSection = new SurveySectionEntity();
        defaultSection.setTitle("Course Evaluation");
        defaultSection.setDescription("Please evaluate your experience with this course");
        defaultSection.setDisplayOrder(0);
        defaultSection.setSurvey(survey);

        // Create default questions
        SurveyQuestionEntity question1 = new SurveyQuestionEntity();
        question1.setQuestionText("How would you rate your overall learning experience?");
        question1.setQuestionType(QuestionTypeEnum.RATING);
        question1.setRequired(true);
        question1.setDisplayOrder(0);
        question1.setMinRating(1);
        question1.setMaxRating(5);
        question1.setLeftLabel("Poor");
        question1.setRightLabel("Excellent");
        question1.setSection(defaultSection);

        SurveyQuestionEntity question2 = new SurveyQuestionEntity();
        question2.setQuestionText("How would you rate the teaching quality?");
        question2.setQuestionType(QuestionTypeEnum.RATING);
        question2.setRequired(true);
        question2.setDisplayOrder(1);
        question2.setMinRating(1);
        question2.setMaxRating(5);
        question2.setLeftLabel("Poor");
        question2.setRightLabel("Excellent");
        question2.setSection(defaultSection);

        SurveyQuestionEntity question3 = new SurveyQuestionEntity();
        question3.setQuestionText("What aspects of the course did you find most valuable?");
        question3.setQuestionType(QuestionTypeEnum.TEXT);
        question3.setRequired(false);
        question3.setDisplayOrder(2);
        question3.setSection(defaultSection);

        SurveyQuestionEntity question4 = new SurveyQuestionEntity();
        question4.setQuestionText("Any additional comments or suggestions?");
        question4.setQuestionType(QuestionTypeEnum.TEXT);
        question4.setRequired(false);
        question4.setDisplayOrder(3);
        question4.setSection(defaultSection);

        defaultSection.getQuestions().add(question1);
        defaultSection.getQuestions().add(question2);
        defaultSection.getQuestions().add(question3);
        defaultSection.getQuestions().add(question4);

        survey.getSections().add(defaultSection);
    }
}