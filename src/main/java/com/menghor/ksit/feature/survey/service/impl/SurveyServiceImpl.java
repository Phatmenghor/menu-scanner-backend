package com.menghor.ksit.feature.survey.service.impl;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.SurveyStatus;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.*;
import com.menghor.ksit.feature.survey.dto.update.SurveyUpdateDto;
import com.menghor.ksit.feature.survey.dto.update.SurveySectionUpdateDto;
import com.menghor.ksit.feature.survey.dto.update.SurveyQuestionUpdateDto;
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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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

        responseDto.setTotalResponses(surveyRepository.countResponsesBySurveyId(mainSurvey.getId()));
        responseDto.setHasUserResponded(false);

        return responseDto;
    }

    @Override
    @Transactional
    public SurveyResponseDto updateMainSurvey(SurveyUpdateDto updateDto) {
        log.info("Updating main survey");

        SurveyEntity mainSurvey = getMainSurveyEntity();

        if (updateDto.getTitle() != null) {
            mainSurvey.setTitle(updateDto.getTitle());
        }
        if (updateDto.getDescription() != null) {
            mainSurvey.setDescription(updateDto.getDescription());
        }

        if (updateDto.getSections() != null) {
            updateSections(mainSurvey, updateDto.getSections());
        }

        SurveyEntity savedSurvey = surveyRepository.save(mainSurvey);
        log.info("Main survey updated successfully with ID: {}", savedSurvey.getId());

        return surveyMapper.toResponseDto(savedSurvey);
    }

    private void updateSections(SurveyEntity survey, List<SurveySectionUpdateDto> sectionDtos) {
        Map<Long, SurveySectionEntity> existingSectionsMap = survey.getSections().stream()
                .filter(s -> s.getId() != null)
                .collect(Collectors.toMap(SurveySectionEntity::getId, s -> s));

        Set<Long> sectionsToKeep = new HashSet<>();
        int maxDisplayOrder = survey.getSections().stream()
                .mapToInt(s -> s.getDisplayOrder() != null ? s.getDisplayOrder() : 1)
                .max()
                .orElse(0);

        for (SurveySectionUpdateDto sectionDto : sectionDtos) {
            SurveySectionEntity section;

            if (sectionDto.getId() != null) {
                section = existingSectionsMap.get(sectionDto.getId());
                if (section == null) {
                    throw new NotFoundException("Section not found with ID: " + sectionDto.getId());
                }
                sectionsToKeep.add(sectionDto.getId());

                if (sectionDto.getTitle() != null) {
                    section.setTitle(sectionDto.getTitle());
                }
                if (sectionDto.getDescription() != null) {
                    section.setDescription(sectionDto.getDescription());
                }
                if (sectionDto.getDisplayOrder() != null) {
                    section.setDisplayOrder(sectionDto.getDisplayOrder());
                }
            } else {
                section = new SurveySectionEntity();
                section.setSurvey(survey);
                survey.getSections().add(section);

                section.setTitle(sectionDto.getTitle());
                section.setDescription(sectionDto.getDescription());
                section.setDisplayOrder(sectionDto.getDisplayOrder() != null ?
                        sectionDto.getDisplayOrder() : (maxDisplayOrder == 0 ? 1 : maxDisplayOrder + 1));
            }

            if (sectionDto.getQuestions() != null) {
                updateQuestions(section, sectionDto.getQuestions());
            }
        }

        List<SurveySectionEntity> sectionsToRemove = survey.getSections().stream()
                .filter(section -> section.getId() != null && !sectionsToKeep.contains(section.getId()))
                .collect(Collectors.toList());

        for (SurveySectionEntity sectionToRemove : sectionsToRemove) {
            survey.getSections().remove(sectionToRemove);
            surveySectionRepository.delete(sectionToRemove);
        }
    }

    private void updateQuestions(SurveySectionEntity section, List<SurveyQuestionUpdateDto> questionDtos) {
        Map<Long, SurveyQuestionEntity> existingQuestionsMap = section.getQuestions().stream()
                .filter(q -> q.getId() != null)
                .collect(Collectors.toMap(SurveyQuestionEntity::getId, q -> q));

        Set<Long> questionsToKeep = new HashSet<>();
        int maxDisplayOrder = section.getQuestions().stream()
                .mapToInt(q -> q.getDisplayOrder() != null ? q.getDisplayOrder() : 1)
                .max()
                .orElse(0);

        for (SurveyQuestionUpdateDto questionDto : questionDtos) {
            SurveyQuestionEntity question;

            if (questionDto.getId() != null) {
                question = existingQuestionsMap.get(questionDto.getId());
                if (question == null) {
                    throw new NotFoundException("Question not found with ID: " + questionDto.getId());
                }
                questionsToKeep.add(questionDto.getId());

                if (questionDto.getQuestionText() != null) {
                    question.setQuestionText(questionDto.getQuestionText());
                }
                if (questionDto.getQuestionType() != null) {
                    question.setQuestionType(questionDto.getQuestionType());
                }
                if (questionDto.getRequired() != null) {
                    question.setRequired(questionDto.getRequired());
                }
                if (questionDto.getDisplayOrder() != null) {
                    question.setDisplayOrder(questionDto.getDisplayOrder());
                }
                if (questionDto.getMinRating() != null) {
                    question.setMinRating(questionDto.getMinRating());
                }
                if (questionDto.getMaxRating() != null) {
                    question.setMaxRating(questionDto.getMaxRating());
                }
                if (questionDto.getLeftLabel() != null) {
                    question.setLeftLabel(questionDto.getLeftLabel());
                }
                if (questionDto.getRightLabel() != null) {
                    question.setRightLabel(questionDto.getRightLabel());
                }
            } else {
                question = new SurveyQuestionEntity();
                question.setSection(section);
                section.getQuestions().add(question);

                question.setQuestionText(questionDto.getQuestionText());
                question.setQuestionType(questionDto.getQuestionType());
                question.setRequired(questionDto.getRequired() != null ? questionDto.getRequired() : false);
                question.setDisplayOrder(questionDto.getDisplayOrder() != null ?
                        questionDto.getDisplayOrder() : (maxDisplayOrder == 0 ? 1 : maxDisplayOrder + 1));
                question.setMinRating(questionDto.getMinRating() != null ? questionDto.getMinRating() : 1);
                question.setMaxRating(questionDto.getMaxRating() != null ? questionDto.getMaxRating() : 5);
                question.setLeftLabel(questionDto.getLeftLabel());
                question.setRightLabel(questionDto.getRightLabel());
            }
        }

        List<SurveyQuestionEntity> questionsToRemove = section.getQuestions().stream()
                .filter(question -> question.getId() != null && !questionsToKeep.contains(question.getId()))
                .collect(Collectors.toList());

        for (SurveyQuestionEntity questionToRemove : questionsToRemove) {
            section.getQuestions().remove(questionToRemove);
            surveyQuestionRepository.delete(questionToRemove);
        }
    }

    @Override
    @Transactional
    public StudentSurveyResponseDto submitSurveyResponseForSchedule(Long scheduleId, SurveyResponseSubmitDto submitDto) {
        log.info("Submitting survey response for schedule ID: {}", scheduleId);

        UserEntity currentUser = securityUtils.getCurrentUser();
        SurveyEntity mainSurvey = getMainSurveyEntity();

        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + scheduleId));

        if (surveyRepository.hasUserRespondedForSchedule(mainSurvey.getId(), currentUser.getId(), scheduleId)) {
            throw new BadRequestException("You have already responded to the survey for this schedule");
        }

        SurveyResponseEntity response = new SurveyResponseEntity();
        response.setSurvey(mainSurvey);
        response.setUser(currentUser);
        response.setSchedule(schedule);
        response.setSubmittedAt(LocalDateTime.now());
        response.setIsCompleted(true);
        response.setOverallComment(submitDto.getOverallComment());
        response.setOverallRating(submitDto.getOverallRating());

        SurveyResponseEntity savedResponse = surveyResponseRepository.save(response);

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

        return responseMapper.toStudentResponseDto(savedResponse);
    }

    @Override
    public StudentSurveyResponseDto getMyResponseForSchedule(Long scheduleId) {
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
        UserEntity currentUser = securityUtils.getCurrentUser();
        SurveyEntity mainSurvey = getMainSurveyEntity();

        boolean hasResponded = surveyRepository.hasUserRespondedForSchedule(
                mainSurvey.getId(), currentUser.getId(), scheduleId);

        return hasResponded ? SurveyStatus.COMPLETED : SurveyStatus.NOT_STARTED;
    }

    @Override
    public CustomPaginationResponseDto<StudentSurveyResponseDto> getScheduleSurveyResponses(Long scheduleId, int pageNo, int pageSize) {
        Pageable pageable = PaginationUtils.createPageable(pageNo, pageSize, "submittedAt", "DESC");
        Page<SurveyResponseEntity> responsePage = surveyResponseRepository.findByScheduleId(scheduleId, pageable);

        return responseMapper.toPaginationResponse(responsePage);
    }

    @Override
    public SurveyResponseDetailDto getStudentResponseDetail(Long responseId) {
        SurveyResponseEntity response = surveyResponseRepository.findById(responseId)
                .orElseThrow(() -> new NotFoundException("Survey response not found with ID: " + responseId));

        return responseMapper.toDetailDto(response);
    }

    @Override
    public SurveyStatisticsDto getSurveyStatistics(Long scheduleId) {
        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + scheduleId));

        SurveyEntity mainSurvey = getMainSurveyEntity();

        Integer totalStudents = scheduleRepository.countStudentsByScheduleId(scheduleId);
        Integer completedResponses = surveyRepository.countResponsesBySurveyIdAndScheduleId(
                mainSurvey.getId(), scheduleId);

        Double completionRate = totalStudents > 0 ?
                (completedResponses.doubleValue() / totalStudents.doubleValue()) * 100 : 0.0;

        Double averageRating = surveyRepository.getAverageRatingForSchedule(mainSurvey.getId(), scheduleId);

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

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeSurveyOnStartup() {
        try {
            Optional<SurveyEntity> existingSurvey = surveyRepository.findByStatus(Status.ACTIVE);
            if (existingSurvey.isEmpty()) {
                createDefaultSurvey();
                log.info("Default survey created successfully on application startup");
            }
        } catch (Exception e) {
            log.error("Error during survey initialization: {}", e.getMessage());
        }
    }

    private void createDefaultSurvey() {
        SurveyEntity mainSurvey = new SurveyEntity();
        mainSurvey.setTitle("Student Course Evaluation Survey");
        mainSurvey.setDescription("Please provide your feedback about your learning experience in this course");
        mainSurvey.setStatus(Status.ACTIVE);
        mainSurvey.setCreatedBy(null);

        createDefaultSurveyContent(mainSurvey);
        surveyRepository.save(mainSurvey);
    }

    @Override
    public Boolean hasUserCompletedSurvey(Long userId, Long scheduleId) {
        SurveyEntity mainSurvey = getMainSurveyEntity();
        return surveyRepository.hasUserRespondedForSchedule(mainSurvey.getId(), userId, scheduleId);
    }

    private SurveyEntity getMainSurveyEntity() {
        return surveyRepository.findByStatus(Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Main survey not found. Please contact administrator."));
    }

    private void createDefaultSurveyContent(SurveyEntity survey) {
        SurveySectionEntity teachingSection = createSection("Teaching Quality",
                "Please evaluate the teaching quality and methods", 1, survey);
        SurveySectionEntity contentSection = createSection("Course Content",
                "Please evaluate the course content and materials", 2, survey);
        SurveySectionEntity environmentSection = createSection("Learning Environment",
                "Please evaluate the learning environment and facilities", 3, survey);
        SurveySectionEntity feedbackSection = createSection("Overall Feedback",
                "Please provide your overall feedback and suggestions", 4, survey);

        addRatingQuestion(teachingSection, "How would you rate the instructor's knowledge of the subject?", 1);
        addRatingQuestion(teachingSection, "How clear and well-organized were the lectures?", 2);
        addRatingQuestion(teachingSection, "How effective was the instructor's teaching method?", 3);
        addRatingQuestion(teachingSection, "How well did the instructor respond to student questions?", 4);

        addRatingQuestion(contentSection, "How relevant was the course content to your learning objectives?", 1);
        addRatingQuestion(contentSection, "How appropriate was the difficulty level of the course?", 2);
        addRatingQuestion(contentSection, "How useful were the course materials and resources?", 3);
        addRatingQuestion(contentSection, "How well were the learning objectives achieved?", 4);

        addRatingQuestion(environmentSection, "How conducive was the classroom environment for learning?", 1);
        addRatingQuestion(environmentSection, "How adequate were the facilities and equipment?", 2);
        addRatingQuestion(environmentSection, "How reasonable was the workload for this course?", 3);

        addRatingQuestion(feedbackSection, "How would you rate your overall learning experience?", 1);
        addTextQuestion(feedbackSection, "What aspects of the course did you find most valuable?", 2);
        addTextQuestion(feedbackSection, "What improvements would you suggest for this course?", 3);
        addTextQuestion(feedbackSection, "Any additional comments or feedback?", 4);

        survey.getSections().addAll(Arrays.asList(teachingSection, contentSection, environmentSection, feedbackSection));
    }

    private SurveySectionEntity createSection(String title, String description, int order, SurveyEntity survey) {
        SurveySectionEntity section = new SurveySectionEntity();
        section.setTitle(title);
        section.setDescription(description);
        section.setDisplayOrder(order);
        section.setSurvey(survey);
        return section;
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