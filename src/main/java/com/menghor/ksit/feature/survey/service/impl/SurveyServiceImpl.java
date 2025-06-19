// 23. SurveyServiceImpl.java
package com.menghor.ksit.feature.survey.service.impl;

import com.menghor.ksit.enumations.StatusSurvey;
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
import com.menghor.ksit.feature.survey.mapper.*;
import com.menghor.ksit.feature.survey.model.*;
import com.menghor.ksit.feature.survey.repository.*;
import com.menghor.ksit.feature.survey.service.SurveyService;
import com.menghor.ksit.feature.survey.specification.*;
import com.menghor.ksit.utils.database.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyServiceImpl implements SurveyService {

    // Repositories
    private final SurveyRepository surveyRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final SurveySectionRepository surveySectionRepository;
    private final ScheduleRepository scheduleRepository;

    // Mappers
    private final SurveyMapper surveyMapper;
    private final SurveySectionMapper sectionMapper;
    private final SurveyQuestionMapper questionMapper;
    private final SurveyResponseMapper responseMapper;
    private final SurveyAnswerMapper answerMapper;

    // Utils
    private final SecurityUtils securityUtils;

    @Override
    public SurveyResponseDto getMainSurvey() {
        log.info("Fetching main survey for admin view");
        SurveyEntity mainSurvey = getMainSurveyEntity();
        log.info("Main survey fetched successfully with ID: {}", mainSurvey.getId());
        return surveyMapper.toResponseDto(mainSurvey);
    }

    @Override
    @Transactional
    public SurveyResponseDto updateMainSurvey(SurveyUpdateDto updateDto) {
        log.info("Updating main survey with soft delete logic");

        SurveyEntity mainSurvey = getMainSurveyEntity();

        // Update basic survey info using consolidated mapper
        surveyMapper.updateSurveyFromDto(updateDto, mainSurvey);

        if (updateDto.getSections() != null) {
            updateSectionsWithSoftDelete(mainSurvey, updateDto.getSections());
        }

        SurveyEntity savedSurvey = surveyRepository.save(mainSurvey);
        log.info("Main survey updated successfully with ID: {}", savedSurvey.getId());

        return surveyMapper.toResponseDto(savedSurvey);
    }

    @Override
    @Transactional
    public SurveyResponseDto deleteSurveySectionAndGetUpdatedSurvey(Long sectionId) {
        log.info("Deleting survey section with ID: {} and returning updated survey", sectionId);

        // Perform the deletion using existing method
        deleteSurveySection(sectionId);

        // Return the updated survey (this will automatically filter out deleted items)
        SurveyResponseDto updatedSurvey = getMainSurvey();
        log.info("Survey section deleted and updated survey returned for section ID: {}", sectionId);

        return updatedSurvey;
    }

    @Override
    @Transactional
    public SurveyResponseDto deleteSurveyQuestionAndGetUpdatedSurvey(Long questionId) {
        log.info("Deleting survey question with ID: {} and returning updated survey", questionId);

        // Perform the deletion using existing method
        deleteSurveyQuestion(questionId);

        // Return the updated survey (this will automatically filter out deleted items)
        SurveyResponseDto updatedSurvey = getMainSurvey();
        log.info("Survey question deleted and updated survey returned for question ID: {}", questionId);

        return updatedSurvey;
    }

    @Transactional
    private void deleteSurveySection(Long sectionId) {
        log.info("Deleting survey section with ID: {}", sectionId);

        SurveySectionEntity section = surveySectionRepository.findById(sectionId)
                .orElseThrow(() -> new NotFoundException("Survey section not found with ID: " + sectionId));

        // Check if section is already deleted
        if (section.getStatus() == StatusSurvey.DELETED) {
            throw new BadRequestException("Section is already deleted");
        }

        // Only allow deletion of ACTIVE sections
        if (section.getStatus() != StatusSurvey.ACTIVE) {
            throw new BadRequestException("Only active sections can be deleted");
        }

        // Set section status to DELETED
        section.setStatus(StatusSurvey.DELETED);

        // Also mark all active questions in this section as deleted
        List<SurveyQuestionEntity> questions = surveyQuestionRepository.findAllBySectionId(sectionId);
        for (SurveyQuestionEntity question : questions) {
            if (question.getStatus() == StatusSurvey.ACTIVE) {
                question.setStatus(StatusSurvey.DELETED);
                surveyQuestionRepository.save(question);
            }
        }

        surveySectionRepository.save(section);
        log.info("Survey section deleted successfully with ID: {}", sectionId);
    }

    @Transactional
    private void deleteSurveyQuestion(Long questionId) {
        log.info("Deleting survey question with ID: {}", questionId);

        SurveyQuestionEntity question = surveyQuestionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Survey question not found with ID: " + questionId));

        // Check if question is already deleted
        if (question.getStatus() == StatusSurvey.DELETED) {
            throw new BadRequestException("Question is already deleted");
        }

        // Only allow deletion of ACTIVE questions
        if (question.getStatus() != StatusSurvey.ACTIVE) {
            throw new BadRequestException("Only active questions can be deleted");
        }

        // Set question status to DELETED
        question.setStatus(StatusSurvey.DELETED);
        surveyQuestionRepository.save(question);

        log.info("Survey question deleted successfully with ID: {}", questionId);
    }

    @Transactional
    protected void updateSectionsWithSoftDelete(SurveyEntity survey, List<SurveySectionUpdateDto> sectionDtos) {
        // Get all existing sections using repository method instead of entity method
        List<SurveySectionEntity> allExistingSections = surveySectionRepository.findAllBySurveyId(survey.getId());
        Map<Long, SurveySectionEntity> existingSectionsMap = allExistingSections.stream()
                .filter(s -> s.getId() != null)
                .collect(Collectors.toMap(SurveySectionEntity::getId, s -> s));

        Set<Long> sectionsToKeep = new HashSet<>();
        int maxDisplayOrder = surveySectionRepository.getMaxDisplayOrderBySurveyId(survey.getId());

        for (SurveySectionUpdateDto sectionDto : sectionDtos) {
            SurveySectionEntity section;

            if (sectionDto.getId() != null) {
                // Updating existing section
                section = existingSectionsMap.get(sectionDto.getId());
                if (section == null) {
                    throw new NotFoundException("Section not found with ID: " + sectionDto.getId());
                }
                sectionsToKeep.add(sectionDto.getId());

                // Reactivate if it was deleted
                if (section.getStatus() == StatusSurvey.DELETED) {
                    section.setStatus(StatusSurvey.ACTIVE);
                }

                // Update using consolidated mapper
                sectionMapper.updateSectionFromDto(sectionDto, section);
            } else {
                // Creating new section using consolidated mapper
                section = sectionMapper.createSectionFromDto(sectionDto);
                section.setSurvey(survey);

                if (section.getDisplayOrder() == null) {
                    section.setDisplayOrder(maxDisplayOrder + 1);
                }

                // Add to survey's sections list
                survey.getSections().add(section);
                surveySectionRepository.save(section);
            }

            if (sectionDto.getQuestions() != null) {
                updateQuestionsWithSoftDelete(section, sectionDto.getQuestions());
            }
        }

        // Mark sections not in the update as deleted (soft delete)
        markUnusedSectionsAsDeleted(allExistingSections, sectionsToKeep);
    }

    @Transactional
    protected void updateQuestionsWithSoftDelete(SurveySectionEntity section, List<SurveyQuestionUpdateDto> questionDtos) {
        // Get all existing questions using repository method instead of entity method
        List<SurveyQuestionEntity> allExistingQuestions = surveyQuestionRepository.findAllBySectionId(section.getId());
        Map<Long, SurveyQuestionEntity> existingQuestionsMap = allExistingQuestions.stream()
                .filter(q -> q.getId() != null)
                .collect(Collectors.toMap(SurveyQuestionEntity::getId, q -> q));

        Set<Long> questionsToKeep = new HashSet<>();
        int maxDisplayOrder = surveyQuestionRepository.getMaxDisplayOrderBySectionId(section.getId());

        for (SurveyQuestionUpdateDto questionDto : questionDtos) {
            SurveyQuestionEntity question;

            if (questionDto.getId() != null) {
                // Updating existing question
                question = existingQuestionsMap.get(questionDto.getId());
                if (question == null) {
                    throw new NotFoundException("Question not found with ID: " + questionDto.getId());
                }
                questionsToKeep.add(questionDto.getId());

                // Reactivate if it was deleted
                if (question.getStatus() == StatusSurvey.DELETED) {
                    question.setStatus(StatusSurvey.ACTIVE);
                }

                // Update using consolidated mapper
                questionMapper.updateQuestionFromDto(questionDto, question);
            } else {
                // Creating new question using consolidated mapper
                question = questionMapper.createQuestionFromDto(questionDto);
                question.setSection(section);

                if (question.getDisplayOrder() == null) {
                    question.setDisplayOrder(maxDisplayOrder + 1);
                }

                // Add to section's questions list
                section.getQuestions().add(question);
                surveyQuestionRepository.save(question);
            }
        }

        // Mark questions not in the update as deleted (soft delete)
        markUnusedQuestionsAsDeleted(allExistingQuestions, questionsToKeep);
    }

    private void markUnusedSectionsAsDeleted(List<SurveySectionEntity> allSections, Set<Long> sectionsToKeep) {
        for (SurveySectionEntity existingSection : allSections) {
            if (existingSection.getId() != null &&
                    !sectionsToKeep.contains(existingSection.getId()) &&
                    existingSection.getStatus() == StatusSurvey.ACTIVE) {

                log.info("Soft deleting section with ID: {}", existingSection.getId());
                existingSection.setStatus(StatusSurvey.DELETED);

                // Also soft delete all questions in this section
                markAllQuestionsInSectionAsDeleted(existingSection.getId());
            }
        }
    }

    private void markUnusedQuestionsAsDeleted(List<SurveyQuestionEntity> allQuestions, Set<Long> questionsToKeep) {
        for (SurveyQuestionEntity existingQuestion : allQuestions) {
            if (existingQuestion.getId() != null &&
                    !questionsToKeep.contains(existingQuestion.getId()) &&
                    existingQuestion.getStatus() == StatusSurvey.ACTIVE) {

                log.info("Soft deleting question with ID: {}", existingQuestion.getId());
                existingQuestion.setStatus(StatusSurvey.DELETED);
            }
        }
    }

    private void markAllQuestionsInSectionAsDeleted(Long sectionId) {
        List<SurveyQuestionEntity> questions = surveyQuestionRepository.findAllBySectionId(sectionId);
        for (SurveyQuestionEntity question : questions) {
            if (question.getStatus() == StatusSurvey.ACTIVE) {
                question.setStatus(StatusSurvey.DELETED);
            }
        }
    }

    @Override
    @Transactional
    public StudentSurveyResponseDto submitSurveyResponseForSchedule(Long scheduleId, SurveyResponseSubmitDto submitDto) {
        log.info("Submitting survey response for schedule ID: {}", scheduleId);

        UserEntity currentUser = securityUtils.getCurrentUser();
        SurveyEntity mainSurvey = getMainSurveyEntity();

        Optional<SurveyResponseEntity> existingResponse = surveyResponseRepository
                .findByUserIdAndScheduleId(currentUser.getId(), scheduleId);

        if (existingResponse.isPresent()) {
            throw new BadRequestException("You have already submitted a survey response for this schedule");
        }

        ScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found with ID: " + scheduleId));

        // Create response using consolidated mapper
        SurveyResponseEntity response = surveyMapper.createResponseFromDto(submitDto);
        response.setSurvey(mainSurvey);
        response.setUser(currentUser);
        response.setSchedule(schedule);

        // Create and store survey snapshot
        String surveySnapshot = responseMapper.createSurveySnapshot(mainSurvey);
        response.setSurveySnapshot(surveySnapshot);
        response.setSurveyTitleSnapshot(mainSurvey.getTitle());
        response.setSurveyDescriptionSnapshot(mainSurvey.getDescription());

        SurveyResponseEntity savedResponse = surveyResponseRepository.save(response);

        // Process answers
        if (submitDto.getAnswers() != null) {
            processAnswers(savedResponse, submitDto);
        }

        return responseMapper.toStudentResponseDto(savedResponse);
    }

    private void processAnswers(SurveyResponseEntity response, SurveyResponseSubmitDto submitDto) {
        for (var answerDto : submitDto.getAnswers()) {
            // Find active question using specification
            SurveyQuestionEntity question = findActiveQuestionById(answerDto.getQuestionId());

            // Create answer using consolidated mapper
            SurveyAnswerEntity answer = answerMapper.createAnswerFromDto(answerDto);
            answer.setResponse(response);
            answer.setQuestion(question);

            surveyAnswerRepository.save(answer);
            response.getAnswers().add(answer);
        }
    }

    private SurveyQuestionEntity findActiveQuestionById(Long questionId) {
        Specification<SurveyQuestionEntity> spec = Specification
                .where(SurveyQuestionSpecification.hasId(questionId))
                .and(SurveyQuestionSpecification.isActive());

        return surveyQuestionRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Active question not found with ID: " + questionId));
    }

    private boolean hasUserRespondedToSurvey(Long userId, Long scheduleId, Long surveyId) {
        return surveyResponseRepository.existsByUserIdAndScheduleIdAndSurveyId(userId, scheduleId, surveyId);
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
    public SurveyResponseDetailDto getStudentResponseDetail(Long responseId) {
        SurveyResponseEntity response = surveyResponseRepository.findById(responseId)
                .orElseThrow(() -> new NotFoundException("Survey response not found with ID: " + responseId));

        return responseMapper.toDetailDto(response);
    }

    @Override
    public Boolean hasUserCompletedSurvey(Long userId, Long scheduleId) {
        SurveyEntity mainSurvey = getMainSurveyEntity();
        return hasUserRespondedToSurvey(userId, scheduleId, mainSurvey.getId());
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeSurveyOnStartup() {
        try {
            // Use specification to find active survey
            Specification<SurveyEntity> spec = SurveySpecification.isActive();
            Optional<SurveyEntity> existingSurvey = surveyRepository.findOne(spec);

            if (existingSurvey.isEmpty()) {
                SurveyEntity defaultSurvey = createDefaultSurvey();
                surveyRepository.save(defaultSurvey);
                log.info("Default survey created successfully on application startup");
            }
        } catch (Exception e) {
            log.error("Error during survey initialization: {}", e.getMessage());
        }
    }

    private SurveyEntity createDefaultSurvey() {
        SurveyEntity mainSurvey = surveyMapper.createDefaultSurvey();
        createDefaultSurveyContent(mainSurvey);
        return mainSurvey;
    }

    private SurveyEntity getMainSurveyEntity() {
        Specification<SurveyEntity> spec = SurveySpecification.isActive();
        return surveyRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Main survey not found. Please contact administrator."));
    }

    private void createDefaultSurveyContent(SurveyEntity survey) {
        // Create only 2 sections
        SurveySectionEntity teachingSection = createDefaultSection(survey, "Teaching Quality & Course Evaluation",
                "Please evaluate the teaching quality, methods, and course content", 1);
        SurveySectionEntity feedbackSection = createDefaultSection(survey, "Overall Feedback",
                "Please provide your overall feedback and suggestions", 2);

        // Add 8 questions to first section and default questions to second section
        addTeachingQuestionsWithEight(teachingSection);
        addDefaultFeedbackQuestions(feedbackSection);

        survey.getSections().addAll(Arrays.asList(teachingSection, feedbackSection));
    }

    private SurveySectionEntity createDefaultSection(SurveyEntity survey, String title, String description, int order) {
        SurveySectionEntity section = sectionMapper.createSectionWithDefaults(title, description, order);
        section.setSurvey(survey);
        return section;
    }

    private void addTeachingQuestionsWithEight(SurveySectionEntity section) {
        String[] questions = {
                "How would you rate the instructor's knowledge of the subject?",
                "How clear and well-organized were the lectures?",
                "How effective was the instructor's teaching method?",
                "How well did the instructor respond to student questions?",
                "How relevant was the course content to your learning objectives?",
                "How appropriate was the difficulty level of the course?",
                "How useful were the course materials and resources?",
                "How conducive was the classroom environment for learning?"
        };

        for (int i = 0; i < questions.length; i++) {
            SurveyQuestionEntity question = questionMapper.createRatingQuestion(questions[i], i + 1);
            question.setSection(section);
            section.getQuestions().add(question);
        }
    }

    private void addDefaultFeedbackQuestions(SurveySectionEntity section) {
        // Mix of rating and text questions
        SurveyQuestionEntity ratingQuestion = questionMapper.createRatingQuestion(
                "How would you rate your overall learning experience?", 1);
        ratingQuestion.setSection(section);
        section.getQuestions().add(ratingQuestion);

        String[] textQuestions = {
                "What aspects of the course did you find most valuable?",
                "What improvements would you suggest for this course?",
                "Any additional comments or feedback?"
        };

        for (int i = 0; i < textQuestions.length; i++) {
            SurveyQuestionEntity question = questionMapper.createTextQuestion(textQuestions[i], i + 2);
            question.setSection(section);
            section.getQuestions().add(question);
        }
    }
}