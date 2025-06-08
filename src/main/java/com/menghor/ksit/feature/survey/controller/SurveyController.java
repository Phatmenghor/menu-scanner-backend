package com.menghor.ksit.feature.survey.controller;

import com.menghor.ksit.enumations.SurveyStatus;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.StudentSurveyResponseDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyResponseDetailDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyResponseDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyStatisticsDto;
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

    /**
     * Get active survey for a specific schedule
     */
    @GetMapping("/schedule/{scheduleId}")
    public ApiResponse<SurveyResponseDto> getActiveSurveyForSchedule(@PathVariable Long scheduleId) {
        log.info("Fetching active survey for schedule ID: {}", scheduleId);
        SurveyResponseDto response = surveyService.getActiveSurveyForSchedule(scheduleId);
        log.info("Active survey fetched successfully for schedule: {}", scheduleId);
        return ApiResponse.success("Survey fetched successfully", response);
    }

    /**
     * Update survey content (Admin/Staff only)
     */
    @PutMapping
    public ApiResponse<SurveyResponseDto> updateSurvey(@Valid @RequestBody SurveyUpdateDto updateDto) {
        log.info("Updating survey with title: {}", updateDto.getTitle());
        SurveyResponseDto response = surveyService.updateSurvey(updateDto);
        log.info("Survey updated successfully");
        return ApiResponse.success("Survey updated successfully", response);
    }

    /**
     * Submit survey response for a specific schedule
     */
    @PostMapping("/schedule/{scheduleId}/submit")
    public ApiResponse<StudentSurveyResponseDto> submitSurveyResponse(
            @PathVariable Long scheduleId,
            @Valid @RequestBody SurveyResponseSubmitDto submitDto) {
        log.info("Submitting survey response for schedule ID: {}", scheduleId);
        StudentSurveyResponseDto response = surveyService.submitSurveyResponseForSchedule(scheduleId, submitDto);
        log.info("Survey response submitted successfully for schedule: {}", scheduleId);
        return ApiResponse.success("Survey response submitted successfully", response);
    }

    /**
     * Get my response for a specific schedule
     */
    @GetMapping("/schedule/{scheduleId}/my-response")
    public ApiResponse<StudentSurveyResponseDto> getMyResponseForSchedule(@PathVariable Long scheduleId) {
        log.info("Fetching current user's survey response for schedule ID: {}", scheduleId);
        StudentSurveyResponseDto response = surveyService.getMyResponseForSchedule(scheduleId);
        log.info("User's survey response fetched successfully for schedule: {}", scheduleId);
        return ApiResponse.success("Your survey response fetched successfully", response);
    }

    /**
     * Get survey status for a specific schedule
     */
    @GetMapping("/schedule/{scheduleId}/status")
    public ApiResponse<SurveyStatus> getSurveyStatusForSchedule(@PathVariable Long scheduleId) {
        log.info("Getting survey status for schedule ID: {}", scheduleId);
        SurveyStatus status = surveyService.getSurveyStatusForSchedule(scheduleId);
        log.info("Survey status retrieved: {} for schedule: {}", status, scheduleId);
        return ApiResponse.success("Survey status retrieved successfully", status);
    }

    /**
     * Get all survey responses (Admin/Staff only)
     */
    @GetMapping("/responses")
    public ApiResponse<CustomPaginationResponseDto<StudentSurveyResponseDto>> getAllResponses(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long scheduleId) {
        log.info("Fetching all survey responses - page: {}, size: {}, scheduleId: {}", pageNo, pageSize, scheduleId);
        CustomPaginationResponseDto<StudentSurveyResponseDto> response =
                surveyService.getAllResponses(pageNo, pageSize, scheduleId);
        log.info("Survey responses fetched successfully. Total elements: {}", response.getTotalElements());
        return ApiResponse.success("Survey responses fetched successfully", response);
    }

    /**
     * Get survey responses for a specific schedule (Admin/Staff only)
     */
    @GetMapping("/schedule/{scheduleId}/responses")
    public ApiResponse<CustomPaginationResponseDto<StudentSurveyResponseDto>> getScheduleSurveyResponses(
            @PathVariable Long scheduleId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Fetching survey responses for schedule {} - page: {}, size: {}", scheduleId, pageNo, pageSize);
        CustomPaginationResponseDto<StudentSurveyResponseDto> response =
                surveyService.getScheduleSurveyResponses(scheduleId, pageNo, pageSize);
        log.info("Schedule survey responses fetched successfully. Total elements: {}", response.getTotalElements());
        return ApiResponse.success("Schedule survey responses fetched successfully", response);
    }

    /**
     * Get detailed survey response (Admin/Staff only)
     */
    @GetMapping("/response/{responseId}/detail")
    public ApiResponse<SurveyResponseDetailDto> getStudentResponseDetail(@PathVariable Long responseId) {
        log.info("Fetching detailed survey response for ID: {}", responseId);
        SurveyResponseDetailDto response = surveyService.getStudentResponseDetail(responseId);
        log.info("Detailed survey response fetched successfully for ID: {}", responseId);
        return ApiResponse.success("Survey response detail fetched successfully", response);
    }

    /**
     * Get survey statistics for a schedule (Admin/Staff only)
     */
    @GetMapping("/schedule/{scheduleId}/statistics")
    public ApiResponse<SurveyStatisticsDto> getSurveyStatistics(@PathVariable Long scheduleId) {
        log.info("Fetching survey statistics for schedule ID: {}", scheduleId);
        SurveyStatisticsDto statistics = surveyService.getSurveyStatistics(scheduleId);
        log.info("Survey statistics fetched successfully for schedule: {}", scheduleId);
        return ApiResponse.success("Survey statistics fetched successfully", statistics);
    }

    /**
     * Initialize default survey (Admin only)
     */
    @PostMapping("/initialize")
    public ApiResponse<String> initializeDefaultSurvey() {
        log.info("Initializing default survey");
        surveyService.initializeDefaultSurvey();
        log.info("Default survey initialized successfully");
        return ApiResponse.success("Default survey initialized successfully", "Survey is ready for use");
    }
}