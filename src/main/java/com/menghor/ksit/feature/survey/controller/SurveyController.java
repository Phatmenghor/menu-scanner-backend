package com.menghor.ksit.feature.survey.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.StudentSurveyResponseDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyResponseDto;
import com.menghor.ksit.feature.survey.dto.update.SurveyUpdateDto;
import com.menghor.ksit.feature.survey.service.SurveyService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/survey")
@RequiredArgsConstructor
public class SurveyController {
    
    private final SurveyService surveyService;
    
    @GetMapping
    public ApiResponse<SurveyResponseDto> getActiveSurvey() {
        log.info("Fetching active survey");
        SurveyResponseDto response = surveyService.getActiveSurvey();
        log.info("Active survey fetched successfully");
        return ApiResponse.success("Survey fetched successfully", response);
    }
    
    @PutMapping
    public ApiResponse<SurveyResponseDto> updateSurvey(@Valid @RequestBody SurveyUpdateDto updateDto) {
        log.info("Updating survey with title: {}", updateDto.getTitle());
        SurveyResponseDto response = surveyService.updateSurvey(updateDto);
        log.info("Survey updated successfully");
        return ApiResponse.success("Survey updated successfully", response);
    }
    
    @PostMapping("/submit")
    public ApiResponse<StudentSurveyResponseDto> submitSurveyResponse(
            @Valid @RequestBody SurveyResponseSubmitDto submitDto) {
        log.info("Submitting survey response");
        StudentSurveyResponseDto response = surveyService.submitSurveyResponse(submitDto);
        log.info("Survey response submitted successfully");
        return ApiResponse.success("Survey response submitted successfully", response);
    }
    
    @GetMapping("/responses")
    public ApiResponse<CustomPaginationResponseDto<StudentSurveyResponseDto>> getAllResponses(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Fetching all survey responses - page: {}, size: {}", pageNo, pageSize);
        CustomPaginationResponseDto<StudentSurveyResponseDto> response = 
            surveyService.getAllResponses(pageNo, pageSize);
        log.info("Survey responses fetched successfully. Total elements: {}", response.getTotalElements());
        return ApiResponse.success("Survey responses fetched successfully", response);
    }
    
    @GetMapping("/my-response")
    public ApiResponse<StudentSurveyResponseDto> getMyResponse() {
        log.info("Fetching current user's survey response");
        StudentSurveyResponseDto response = surveyService.getMyResponse();
        log.info("User's survey response fetched successfully");
        return ApiResponse.success("Your survey response fetched successfully", response);
    }
    
    @PostMapping("/initialize")
    public ApiResponse<String> initializeDefaultSurvey() {
        log.info("Initializing default survey");
        surveyService.initializeDefaultSurvey();
        log.info("Default survey initialized successfully");
        return ApiResponse.success("Default survey initialized successfully", "Survey is ready for use");
    }
}