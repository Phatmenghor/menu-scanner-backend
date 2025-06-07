package com.menghor.ksit.feature.survey.service.impl;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.StudentSurveyResponseDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyResponseDto;
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
    private final SurveyMapper surveyMapper;
    private final SurveyResponseMapper responseMapper;
    private final SecurityUtils securityUtils;
    
    @Override
    public SurveyResponseDto getActiveSurvey() {
        log.info("Fetching active survey");
        
        SurveyEntity survey = getOrCreateActiveSurvey();
        SurveyResponseDto responseDto = surveyMapper.toResponseDto(survey);
        
        // Add additional data
        responseDto.setTotalResponses(surveyRepository.countResponsesBySurveyId(survey.getId()));
        
        // Check if current user has responded (for students)
        try {
            UserEntity currentUser = securityUtils.getCurrentUser();
            responseDto.setHasUserResponded(surveyRepository.hasUserResponded(survey.getId(), currentUser.getId()));
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
    public StudentSurveyResponseDto submitSurveyResponse(SurveyResponseSubmitDto submitDto) {
        log.info("Submitting survey response");
        
        UserEntity currentUser = securityUtils.getCurrentUser();
        SurveyEntity survey = getOrCreateActiveSurvey();
        
        // Check if user already responded
        if (surveyRepository.hasUserResponded(survey.getId(), currentUser.getId())) {
            throw new BadRequestException("You have already responded to this survey");
        }
        
        // Create survey response
        SurveyResponseEntity response = new SurveyResponseEntity();
        response.setSurvey(survey);
        response.setUser(currentUser);
        response.setSubmittedAt(LocalDateTime.now());
        response.setIsCompleted(true);
        
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
        
        log.info("Survey response submitted successfully with ID: {}", savedResponse.getId());
        return responseMapper.toStudentResponseDto(savedResponse);
    }
    
    @Override
    public CustomPaginationResponseDto<StudentSurveyResponseDto> getAllResponses(int pageNo, int pageSize) {
        log.info("Fetching all survey responses - page: {}, size: {}", pageNo, pageSize);
        
        SurveyEntity survey = getOrCreateActiveSurvey();
        
        Pageable pageable = PaginationUtils.createPageable(pageNo, pageSize, "submittedAt", "DESC");
        Page<SurveyResponseEntity> responsePage = surveyResponseRepository.findBySurveyId(survey.getId(), pageable);
        
        return responseMapper.toPaginationResponse(responsePage);
    }
    
    @Override
    public StudentSurveyResponseDto getMyResponse() {
        log.info("Fetching current user's survey response");
        
        UserEntity currentUser = securityUtils.getCurrentUser();
        SurveyEntity survey = getOrCreateActiveSurvey();
        
        Pageable pageable = PaginationUtils.createPageable(1, 1);
        Page<SurveyResponseEntity> responsePage = surveyResponseRepository.findByUserId(currentUser.getId(), pageable);
        
        if (responsePage.isEmpty()) {
            throw new NotFoundException("You have not responded to the survey yet");
        }
        
        return responseMapper.toStudentResponseDto(responsePage.getContent().get(0));
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
}
