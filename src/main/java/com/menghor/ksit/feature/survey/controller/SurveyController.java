package com.menghor.ksit.feature.survey.controller;

import com.menghor.ksit.enumations.SurveyStatus;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.*;
import com.menghor.ksit.feature.survey.dto.update.SurveyUpdateDto;
import com.menghor.ksit.feature.survey.service.SurveyService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    /**
     * Get main survey for admin management
     */
    @GetMapping("/main")
    public ApiResponse<SurveyResponseDto> getMainSurvey() {
        log.info("Fetching main survey for admin");
        SurveyResponseDto response = surveyService.getMainSurvey();
        log.info("Main survey fetched successfully");
        return ApiResponse.success("Main survey fetched successfully", response);
    }

    /**
     * Update main survey content (Admin/Staff only)
     */
    @PutMapping("/main")
    public ApiResponse<SurveyResponseDto> updateMainSurvey(@Valid @RequestBody SurveyUpdateDto updateDto) {
        log.info("Updating main survey with title: {}", updateDto.getTitle());
        SurveyResponseDto response = surveyService.updateMainSurvey(updateDto);
        log.info("Main survey updated successfully");
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
     * Get all survey responses for a specific schedule (Admin/Staff only)
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

}