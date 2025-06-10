package com.menghor.ksit.feature.survey.service.impl;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.SurveyStatus;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.dto.resposne.UserBasicInfoDto;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        // Verify schedule exists
        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + scheduleId));

        UserEntity currentUser = securityUtils.getCurrentUser();
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

        // Build detailed response DTO manually for complete control
        SurveyResponseDetailDto detailDto = new SurveyResponseDetailDto();
        detailDto.setId(response.getId());
        detailDto.setSurveyId(response.getSurvey().getId());
        detailDto.setSurveyTitle(response.getSurvey().getTitle());
        detailDto.setSubmittedAt(response.getSubmittedAt());
        detailDto.setIsCompleted(response.getIsCompleted());
        detailDto.setCreatedAt(response.getCreatedAt());

        // Map student info
        UserBasicInfoDto studentInfo = new UserBasicInfoDto();
        studentInfo.setId(response.getUser().getId());
        studentInfo.setUsername(response.getUser().getUsername());
        studentInfo.setEnglishFirstName(response.getUser().getEnglishFirstName());
        studentInfo.setEnglishLastName(response.getUser().getEnglishLastName());
        studentInfo.setKhmerFirstName(response.getUser().getKhmerFirstName());
        studentInfo.setKhmerLastName(response.getUser().getKhmerLastName());
        studentInfo.setIdentifyNumber(response.getUser().getIdentifyNumber());
        detailDto.setStudent(studentInfo);

        // Map schedule info
        ScheduleBasicInfoDto scheduleInfo = new ScheduleBasicInfoDto();
        scheduleInfo.setId(response.getSchedule().getId());
        scheduleInfo.setCourseName(response.getSchedule().getCourse().getNameEn());
        scheduleInfo.setCourseCode(response.getSchedule().getCourse().getCode());
        scheduleInfo.setClassName(response.getSchedule().getClasses().getCode());
        scheduleInfo.setRoomName(response.getSchedule().getRoom().getName());
        scheduleInfo.setDayOfWeek(response.getSchedule().getDay());
        scheduleInfo.setStartTime(response.getSchedule().getStartTime());
        scheduleInfo.setEndTime(response.getSchedule().getEndTime());

        // Set teacher name
        UserEntity teacher = response.getSchedule().getUser();
        if (teacher != null) {
            String teacherName = getFormattedUserName(teacher);
            scheduleInfo.setTeacherName(teacherName);
        }

        detailDto.setSchedule(scheduleInfo);

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
        statistics.setCompletionRate(Math.round(completionRate * 100.0) / 100.0);
        statistics.setAverageRating(averageRating != null ? Math.round(averageRating * 100.0) / 100.0 : null);
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

    @Override
    public List<StudentScheduleWithSurveyDto> getMySchedulesWithSurveyStatus() {
        log.info("Fetching current user's schedules with survey status");

        UserEntity currentUser = securityUtils.getCurrentUser();
        SurveyEntity mainSurvey = getMainSurveyEntity();

        // Get user's schedules
        Pageable pageable = PaginationUtils.createPageable(1, 1000, "startTime", "ASC");
        Page<ScheduleEntity> schedulePage = scheduleRepository.findByStudentId(currentUser.getId(), pageable);

        return schedulePage.getContent().stream()
                .map(schedule -> {
                    StudentScheduleWithSurveyDto dto = new StudentScheduleWithSurveyDto();

                    // Basic schedule info
                    dto.setScheduleId(schedule.getId());
                    dto.setCourseName(schedule.getCourse().getNameEn());
                    dto.setCourseCode(schedule.getCourse().getCode());
                    dto.setClassName(schedule.getClasses().getCode());
                    dto.setRoomName(schedule.getRoom().getName());
                    dto.setDayOfWeek(schedule.getDay());
                    dto.setStartTime(schedule.getStartTime());
                    dto.setEndTime(schedule.getEndTime());

                    // Teacher info
                    UserEntity teacher = schedule.getUser();
                    if (teacher != null) {
                        dto.setTeacherName(getFormattedUserName(teacher));
                        dto.setTeacherEmail(teacher.getEmail());
                    }

                    // Semester info
                    if (schedule.getSemester() != null) {
                        dto.setSemester(schedule.getSemester().getSemester().name());
                        dto.setAcademicYear(schedule.getSemester().getAcademyYear());
                        dto.setStartDate(schedule.getSemester().getStartDate());
                        dto.setEndDate(schedule.getSemester().getEndDate());
                    }

                    // Survey status
                    boolean hasResponded = surveyRepository.hasUserRespondedForSchedule(
                            mainSurvey.getId(), currentUser.getId(), schedule.getId());
                    dto.setSurveyStatus(hasResponded ? SurveyStatus.COMPLETED : SurveyStatus.NOT_STARTED);

                    // If completed, get submission date and response ID
                    if (hasResponded) {
                        Optional<SurveyResponseEntity> responseOpt = surveyResponseRepository
                                .findByUserIdAndScheduleId(currentUser.getId(), schedule.getId());
                        if (responseOpt.isPresent()) {
                            dto.setSurveySubmittedDate(responseOpt.get().getSubmittedAt());
                            dto.setSurveyResponseId(responseOpt.get().getId());
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public SurveyDashboardDto getSurveyDashboard() {
        log.info("Fetching survey dashboard data");

        SurveyEntity mainSurvey = getMainSurveyEntity();

        // Get total responses across all schedules
        Integer totalResponses = surveyRepository.countResponsesBySurveyId(mainSurvey.getId());

        // Get total students across all schedules
        Long totalStudents = scheduleRepository.countTotalStudents();

        // Calculate overall completion rate
        Double overallCompletionRate = totalStudents > 0 ?
                (totalResponses.doubleValue() / totalStudents.doubleValue()) * 100 : 0.0;

        // Get average rating across all responses
        Double averageRating = surveyRepository.getAverageRatingForSchedule(mainSurvey.getId(), null);

        // Get total schedules
        Long totalSchedules = scheduleRepository.count();

        SurveyDashboardDto dashboard = new SurveyDashboardDto();
        dashboard.setSurveyTitle(mainSurvey.getTitle());
        dashboard.setTotalResponses(totalResponses);
        dashboard.setTotalStudents(totalStudents.intValue());
        dashboard.setTotalSchedules(totalSchedules.intValue());
        dashboard.setOverallCompletionRate(Math.round(overallCompletionRate * 100.0) / 100.0);
        dashboard.setAverageRating(averageRating != null ? Math.round(averageRating * 100.0) / 100.0 : null);
        dashboard.setPendingResponses(totalStudents.intValue() - totalResponses);

        return dashboard;
    }

    // Private helper methods

    private SurveyEntity getMainSurveyEntity() {
        return surveyRepository.findByStatus(Status.ACTIVE)
                .orElseThrow(() -> {
                    log.error("Main survey not found. Please initialize the survey first.");
                    return new NotFoundException("Main survey not found. Please contact administrator.");
                });
    }

    private String getFormattedUserName(UserEntity user) {
        if (user.getEnglishFirstName() != null && user.getEnglishLastName() != null) {
            return user.getEnglishFirstName() + " " + user.getEnglishLastName();
        }
        if (user.getKhmerFirstName() != null && user.getKhmerLastName() != null) {
            return user.getKhmerFirstName() + " " + user.getKhmerLastName();
        }
        return user.getUsername();
    }

    private void createDefaultSurveyContent(SurveyEntity survey) {
        // Create Teaching Quality section
        SurveySectionEntity teachingSection = new SurveySectionEntity();
        teachingSection.setTitle("Teaching Quality");
        teachingSection.setDescription("Please evaluate the teaching quality and methods");
        teachingSection.setDisplayOrder(0);
        teachingSection.setSurvey(survey);

        // Create Course Content section
        SurveySectionEntity contentSection = new SurveySectionEntity();
        contentSection.setTitle("Course Content");
        contentSection.setDescription("Please evaluate the course content and materials");
        contentSection.setDisplayOrder(1);
        contentSection.setSurvey(survey);

        // Create Learning Environment section
        SurveySectionEntity environmentSection = new SurveySectionEntity();
        environmentSection.setTitle("Learning Environment");
        environmentSection.setDescription("Please evaluate the learning environment and facilities");
        environmentSection.setDisplayOrder(2);
        environmentSection.setSurvey(survey);

        // Create Overall Feedback section
        SurveySectionEntity feedbackSection = new SurveySectionEntity();
        feedbackSection.setTitle("Overall Feedback");
        feedbackSection.setDescription("Please provide your overall feedback and suggestions");
        feedbackSection.setDisplayOrder(3);
        feedbackSection.setSurvey(survey);

        // Teaching Quality questions
        addRatingQuestion(teachingSection, "How would you rate the instructor's knowledge of the subject?", 0);
        addRatingQuestion(teachingSection, "How clear and well-organized were the lectures?", 1);
        addRatingQuestion(teachingSection, "How effective was the instructor's teaching method?", 2);
        addRatingQuestion(teachingSection, "How well did the instructor respond to student questions?", 3);

        // Course Content questions
        addRatingQuestion(contentSection, "How relevant was the course content to your learning objectives?", 0);
        addRatingQuestion(contentSection, "How appropriate was the difficulty level of the course?", 1);
        addRatingQuestion(contentSection, "How useful were the course materials and resources?", 2);
        addRatingQuestion(contentSection, "How well were the learning objectives achieved?", 3);

        // Learning Environment questions
        addRatingQuestion(environmentSection, "How conducive was the classroom environment for learning?", 0);
        addRatingQuestion(environmentSection, "How adequate were the facilities and equipment?", 1);
        addRatingQuestion(environmentSection, "How reasonable was the workload for this course?", 2);

        // Overall Feedback questions
        addRatingQuestion(feedbackSection, "How would you rate your overall learning experience?", 0);
        addTextQuestion(feedbackSection, "What aspects of the course did you find most valuable?", 1);
        addTextQuestion(feedbackSection, "What improvements would you suggest for this course?", 2);
        addTextQuestion(feedbackSection, "Any additional comments or feedback?", 3);

        survey.getSections().add(teachingSection);
        survey.getSections().add(contentSection);
        survey.getSections().add(environmentSection);
        survey.getSections().add(feedbackSection);
    }

    private void addRatingQuestion(SurveySectionEntity section, String questionText, int order) {
        SurveyQuestionEntity question = new SurveyQuestionEntity();
        question.setQuestionText(questionText);
        question.setQuestionType(QuestionTypeEnum.RATING);
        question.setRequired(true);
        question.setDisplayOrder(order);
        question.setMinRating(1);
        question.setMaxRating(5);
        question.setLeftLabel("Poor");
        question.setRightLabel("Excellent");
        question.setSection(section);
        section.getQuestions().add(question);
    }

    private void addTextQuestion(SurveySectionEntity section, String questionText, int order) {
        SurveyQuestionEntity question = new SurveyQuestionEntity();
        question.setQuestionText(questionText);
        question.setQuestionType(QuestionTypeEnum.TEXT);
        question.setRequired(false);
        question.setDisplayOrder(order);
        question.setSection(section);
        section.getQuestions().add(question);
    }
}
