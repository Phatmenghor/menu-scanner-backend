package com.menghor.ksit.feature.survey.service.impl;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.SurveyStatus;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
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
import java.util.ArrayList;
import java.util.List;
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
    private final UserRepository userRepository;
    private final SurveyMapper surveyMapper;
    private final SurveyResponseMapper responseMapper;
    private final SecurityUtils securityUtils;

    @Override
    public SurveyResponseDto getActiveSurveyForSchedule(Long scheduleId) {
        log.info("Fetching active survey for schedule ID: {}", scheduleId);

        // Verify schedule exists
        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + scheduleId));

        SurveyEntity survey = getOrCreateActiveSurvey();
        SurveyResponseDto responseDto = surveyMapper.toResponseDto(survey);

        // Add schedule-specific data
        responseDto.setTotalResponses(surveyRepository.countResponsesBySurveyIdAndScheduleId(survey.getId(), scheduleId));

        // Check if current user has responded for this schedule
        try {
            UserEntity currentUser = securityUtils.getCurrentUser();
            responseDto.setHasUserResponded(
                    surveyRepository.hasUserRespondedForSchedule(survey.getId(), currentUser.getId(), scheduleId)
            );
        } catch (Exception e) {
            responseDto.setHasUserResponded(false);
        }

        return responseDto;
    }

    @Override
    @Transactional
    public SurveyResponseDto updateSurvey(SurveyUpdateDto updateDto) {
        log.info("Updating survey with title: {}", updateDto.getTitle());

        UserEntity currentUser = securityUtils.getCurrentUser();

        // Check if user has admin/staff privileges
        boolean hasAdminRole = currentUser.getRoles().stream()
                .map(Role::getName)
                .anyMatch(role -> role == RoleEnum.ADMIN || role == RoleEnum.DEVELOPER || role == RoleEnum.STAFF);

        if (!hasAdminRole) {
            throw new BadRequestException("Only admin/staff can update surveys");
        }

        SurveyEntity survey = getOrCreateActiveSurvey();

        // Update basic fields
        survey.setTitle(updateDto.getTitle());
        survey.setDescription(updateDto.getDescription());

        // Clear existing sections and questions
        survey.getSections().clear();
        surveyRepository.save(survey);

        // Add new sections and questions
        if (updateDto.getSections() != null) {
            for (int i = 0; i < updateDto.getSections().size(); i++) {
                var sectionDto = updateDto.getSections().get(i);

                SurveySectionEntity section = new SurveySectionEntity();
                section.setTitle(sectionDto.getTitle());
                section.setDescription(sectionDto.getDescription());
                section.setDisplayOrder(sectionDto.getDisplayOrder() != null ? sectionDto.getDisplayOrder() : i);
                section.setSurvey(survey);

                survey.getSections().add(section);

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

        SurveyEntity savedSurvey = surveyRepository.save(survey);
        log.info("Survey updated successfully with ID: {}", savedSurvey.getId());

        return surveyMapper.toResponseDto(savedSurvey);
    }

    @Override
    @Transactional
    public StudentSurveyResponseDto submitSurveyResponseForSchedule(Long scheduleId, SurveyResponseSubmitDto submitDto) {
        log.info("Submitting survey response for schedule ID: {}", scheduleId);

        UserEntity currentUser = securityUtils.getCurrentUser();
        SurveyEntity survey = getOrCreateActiveSurvey();

        // Verify schedule exists
        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + scheduleId));

        // Check if user already responded for this schedule
        if (surveyRepository.hasUserRespondedForSchedule(survey.getId(), currentUser.getId(), scheduleId)) {
            throw new BadRequestException("You have already responded to the survey for this schedule");
        }

        // Verify student is enrolled in this schedule
        if (!isStudentEnrolledInSchedule(currentUser.getId(), scheduleId)) {
            throw new BadRequestException("You are not enrolled in this schedule");
        }

        // Create survey response
        SurveyResponseEntity response = new SurveyResponseEntity();
        response.setSurvey(survey);
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
    public CustomPaginationResponseDto<StudentSurveyResponseDto> getAllResponses(int pageNo, int pageSize, Long scheduleId) {
        log.info("Fetching all survey responses - page: {}, size: {}, scheduleId: {}", pageNo, pageSize, scheduleId);

        SurveyEntity survey = getOrCreateActiveSurvey();

        Pageable pageable = PaginationUtils.createPageable(pageNo, pageSize, "submittedAt", "DESC");
        Page<SurveyResponseEntity> responsePage;

        if (scheduleId != null) {
            responsePage = surveyResponseRepository.findByScheduleId(scheduleId, pageable);
        } else {
            responsePage = surveyResponseRepository.findBySurveyId(survey.getId(), pageable);
        }

        return responseMapper.toPaginationResponse(responsePage);
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
    public CustomPaginationResponseDto<StudentScheduleWithSurveyDto> getMySchedulesWithSurveyStatus(int pageNo, int pageSize) {
        log.info("Fetching current student's schedules with survey status - page: {}, size: {}", pageNo, pageSize);

        UserEntity currentUser = securityUtils.getCurrentUser();

        // Verify user is a student
        boolean isStudent = currentUser.getRoles().stream()
                .map(Role::getName)
                .anyMatch(role -> role == RoleEnum.STUDENT);

        if (!isStudent) {
            throw new BadRequestException("Only students can access this endpoint");
        }

        // Get student's schedules
        Pageable pageable = PaginationUtils.createPageable(pageNo, pageSize, "startDate", "DESC");
        Page<ScheduleEntity> schedulePage = scheduleRepository.findByStudentId(currentUser.getId(), pageable);

        // Convert to DTOs with survey status
        List<StudentScheduleWithSurveyDto> scheduleWithSurveyList = new ArrayList<>();
        SurveyEntity survey = getOrCreateActiveSurvey();

        for (ScheduleEntity schedule : schedulePage.getContent()) {
            StudentScheduleWithSurveyDto dto = buildStudentScheduleWithSurveyDto(schedule, currentUser, survey);
            scheduleWithSurveyList.add(dto);
        }

        return CustomPaginationResponseDto.<StudentScheduleWithSurveyDto>builder()
                .content(scheduleWithSurveyList)
                .pageNo(schedulePage.getNumber() + 1)
                .pageSize(schedulePage.getSize())
                .totalElements(schedulePage.getTotalElements())
                .totalPages(schedulePage.getTotalPages())
                .last(schedulePage.isLast())
                .build();
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

        // Set student info (you'll need to implement this mapping)
        // detailDto.setStudent(mapToUserBasicInfo(response.getUser()));

        // Set schedule info (you'll need to implement this mapping)
        // detailDto.setSchedule(mapToScheduleBasicInfo(response.getSchedule()));

        // Set answer details
        List<SurveyAnswerDetailDto> answerDetails = new ArrayList<>();
        for (SurveyAnswerEntity answer : response.getAnswers()) {
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

            answerDetails.add(answerDetailDto);
        }
        detailDto.setAnswerDetails(answerDetails);

        return detailDto;
    }

    @Override
    public CustomPaginationResponseDto<StudentSurveyResponseDto> getScheduleSurveyResponses(Long scheduleId, int pageNo, int pageSize) {
        log.info("Fetching survey responses for schedule ID: {} - page: {}, size: {}", scheduleId, pageNo, pageSize);

        Pageable pageable = PaginationUtils.createPageable(pageNo, pageSize, "submittedAt", "DESC");
        Page<SurveyResponseEntity> responsePage = surveyResponseRepository.findByScheduleId(scheduleId, pageable);

        return responseMapper.toPaginationResponse(responsePage);
    }

    @Override
    public SurveyStatus getSurveyStatusForSchedule(Long scheduleId) {
        log.info("Getting survey status for schedule ID: {}", scheduleId);

        UserEntity currentUser = securityUtils.getCurrentUser();
        SurveyEntity survey = getOrCreateActiveSurvey();

        boolean hasResponded = surveyRepository.hasUserRespondedForSchedule(
                survey.getId(), currentUser.getId(), scheduleId);

        return hasResponded ? SurveyStatus.COMPLETED : SurveyStatus.NOT_STARTED;
    }

    @Override
    public SurveyStatisticsDto getSurveyStatistics(Long scheduleId) {
        log.info("Getting survey statistics for schedule ID: {}", scheduleId);

        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + scheduleId));

        SurveyEntity survey = getOrCreateActiveSurvey();

        // Get total students in the schedule
        Integer totalStudents = scheduleRepository.countStudentsByScheduleId(scheduleId);

        // Get completed responses
        Integer completedResponses = surveyRepository.countResponsesBySurveyIdAndScheduleId(
                survey.getId(), scheduleId);

        // Calculate completion rate
        Double completionRate = totalStudents > 0 ?
                (completedResponses.doubleValue() / totalStudents.doubleValue()) * 100 : 0.0;

        // Get average rating
        Double averageRating = surveyRepository.getAverageRatingForSchedule(survey.getId(), scheduleId);

        // Get total questions count
        Integer totalQuestions = survey.getSections().stream()
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
    public void initializeDefaultSurvey() {
        log.info("Initializing default survey if not exists");

        Optional<SurveyEntity> existingSurvey = surveyRepository.findByStatus(Status.ACTIVE);
        if (existingSurvey.isPresent()) {
            log.info("Active survey already exists with ID: {}", existingSurvey.get().getId());
            return;
        }

        // Create default survey with system user
        SurveyEntity defaultSurvey = new SurveyEntity();
        defaultSurvey.setTitle("Student Feedback Survey");
        defaultSurvey.setDescription("Please provide your feedback about your learning experience");
        defaultSurvey.setStatus(Status.ACTIVE);

        // Set a system user as creator (you might need to adjust this based on your user setup)
        try {
            UserEntity systemUser = securityUtils.getCurrentUser();
            defaultSurvey.setCreatedBy(systemUser);
        } catch (Exception e) {
            log.warn("Could not set current user as survey creator: {}", e.getMessage());
            // You might want to create a system user or handle this differently
        }

        // Create a default section
        SurveySectionEntity defaultSection = new SurveySectionEntity();
        defaultSection.setTitle("General Feedback");
        defaultSection.setDescription("Your general feedback about the course");
        defaultSection.setDisplayOrder(0);
        defaultSection.setSurvey(defaultSurvey);

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
        question2.setQuestionText("What aspects of the course did you find most valuable?");
        question2.setQuestionType(QuestionTypeEnum.TEXT);
        question2.setRequired(false);
        question2.setDisplayOrder(1);
        question2.setSection(defaultSection);

        SurveyQuestionEntity question3 = new SurveyQuestionEntity();
        question3.setQuestionText("How would you rate the teaching quality?");
        question3.setQuestionType(QuestionTypeEnum.RATING);
        question3.setRequired(true);
        question3.setDisplayOrder(2);
        question3.setMinRating(1);
        question3.setMaxRating(5);
        question3.setLeftLabel("Poor");
        question3.setRightLabel("Excellent");
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

        defaultSurvey.getSections().add(defaultSection);

        SurveyEntity savedSurvey = surveyRepository.save(defaultSurvey);
        log.info("Default survey created successfully with ID: {}", savedSurvey.getId());
    }

    // Private helper methods

    private SurveyEntity getOrCreateActiveSurvey() {
        Optional<SurveyEntity> activeSurvey = surveyRepository.findByStatus(Status.ACTIVE);
        if (activeSurvey.isPresent()) {
            return activeSurvey.get();
        }

        // If no active survey exists, initialize one
        initializeDefaultSurvey();
        return surveyRepository.findByStatus(Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Could not create or find active survey"));
    }

    private boolean isStudentEnrolledInSchedule(Long studentId, Long scheduleId) {
        // Check if the student's class is associated with this schedule
        return scheduleRepository.existsByIdAndClassesStudentsId(scheduleId, studentId);
    }

    private StudentScheduleWithSurveyDto buildStudentScheduleWithSurveyDto(
            ScheduleEntity schedule, UserEntity student, SurveyEntity survey) {

        // Check if student has completed survey for this schedule
        boolean hasCompleted = surveyRepository.hasUserRespondedForSchedule(
                survey.getId(), student.getId(), schedule.getId());

        SurveyStatus surveyStatus = hasCompleted ? SurveyStatus.COMPLETED : SurveyStatus.NOT_STARTED;

        // Get survey response details if completed
        Long surveyResponseId = null;
        LocalDateTime surveySubmittedDate = null;

        if (hasCompleted) {
            Optional<SurveyResponseEntity> responseOpt = surveyResponseRepository
                    .findByUserIdAndScheduleId(student.getId(), schedule.getId());
            if (responseOpt.isPresent()) {
                SurveyResponseEntity response = responseOpt.get();
                surveyResponseId = response.getId();
                surveySubmittedDate = response.getSubmittedAt();
            }
        }

        return StudentScheduleWithSurveyDto.builder()
                .scheduleId(schedule.getId())
                .courseName(schedule.getCourse().getCode())
                .courseCode(schedule.getCourse().getCode())
                .teacherName(schedule.getUser().getKhmerFirstName() + " " + schedule.getUser().getKhmerLastName())
                .teacherEmail(schedule.getUser().getEmail())
                .className(schedule.getClasses().getCode())
                .roomName(schedule.getRoom().getName())
                .roomCode(schedule.getRoom().getName())
                .dayOfWeek(schedule.getDay())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .startDate(schedule.getSemester().getStartDate())
                .endDate(schedule.getSemester().getEndDate())
                .surveyStatus(surveyStatus)
                .surveySubmittedDate(surveySubmittedDate)
                .surveyResponseId(surveyResponseId)
                .semester(schedule.getSemester() != null ? schedule.getSemester().getSemester().toString() : null)
                .academicYear(schedule.getSemester().getAcademyYear())
                .build();
    }
}
