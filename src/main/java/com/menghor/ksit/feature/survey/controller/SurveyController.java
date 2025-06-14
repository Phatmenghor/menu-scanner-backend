package com.menghor.ksit.feature.survey.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.survey.dto.filter.SurveyReportFilterDto;
import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.*;
import com.menghor.ksit.feature.survey.dto.update.SurveyUpdateDto;
import com.menghor.ksit.feature.survey.service.SurveyService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * Delete survey section by ID (Admin/Staff only)
     * This sets the section status to DELETED and returns updated survey
     */
    @DeleteMapping("/section/{sectionId}")
    public ApiResponse<SurveyResponseDto> deleteSurveySection(@PathVariable Long sectionId) {
        log.info("Deleting survey section with ID: {}", sectionId);
        SurveyResponseDto updatedSurvey = surveyService.deleteSurveySectionAndGetUpdatedSurvey(sectionId);
        log.info("Survey section deleted successfully with ID: {}", sectionId);
        return ApiResponse.success("Survey section deleted successfully", updatedSurvey);
    }

    /**
     * Delete survey question by ID (Admin/Staff only)
     * This sets the question status to DELETED and returns updated survey
     */
    @DeleteMapping("/question/{questionId}")
    public ApiResponse<SurveyResponseDto> deleteSurveyQuestion(@PathVariable Long questionId) {
        log.info("Deleting survey question with ID: {}", questionId);
        SurveyResponseDto updatedSurvey = surveyService.deleteSurveyQuestionAndGetUpdatedSurvey(questionId);
        log.info("Survey question deleted successfully with ID: {}", questionId);
        return ApiResponse.success("Survey question deleted successfully", updatedSurvey);
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
     * Get detailed survey response (Admin/Staff only)
     */
    @GetMapping("/response/{responseId}/detail")
    public ApiResponse<SurveyResponseDetailDto> getStudentResponseDetail(@PathVariable Long responseId) {
        log.info("Fetching detailed survey response for ID: {}", responseId);
        SurveyResponseDetailDto response = surveyService.getStudentResponseDetail(responseId);
        log.info("Detailed survey response fetched successfully for ID: {}", responseId);
        return ApiResponse.success("Survey response detail fetched successfully", response);
    }

//    /**
//     * Get survey responses report with pagination (for web table preview)
//     * Admin/Staff only
//     */
//    @PostMapping("/reports/preview")
//    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'DEVELOPER')")
//    public ApiResponse<SurveyReportResponseDto> getSurveyReportPreview(
//            @Valid @RequestBody SurveyReportFilterDto filterDto) {
//        log.info("Fetching survey report preview with filters: {}", filterDto);
//        SurveyReportResponseDto response = surveyReportService.getSurveyReportWithPagination(filterDto);
//        log.info("Survey report preview fetched successfully. Total: {}", response.getTotalElements());
//        return ApiResponse.success("Survey report preview fetched successfully", response);
//    }
//
//    /**
//     * Get all survey responses for export/preview (same data structure)
//     * Admin/Staff only
//     */
//    @PostMapping("/reports/export")
//    public ApiResponse<List<SurveyReportRowDto>> getSurveyReportForExport(
//            @Valid @RequestBody SurveyReportFilterDto filterDto) {
//        log.info("Fetching survey report for export with filters: {}", filterDto);
//        List<SurveyReportRowDto> response = surveyReportService.getSurveyReportForExport(filterDto);
//        log.info("Survey report for export fetched successfully. Total rows: {}", response.size());
//        return ApiResponse.success("Survey report for export fetched successfully", response);
//    }
//
//    /**
//     * Get survey report headers (for dynamic table structure)
//     * Admin/Staff only
//     */
//    @GetMapping("/reports/headers")
//    public ApiResponse<List<SurveyReportHeaderDto>> getSurveyReportHeaders() {
//        log.info("Fetching survey report headers");
//        List<SurveyReportHeaderDto> headers = surveyReportService.getSurveyReportHeaders();
//        log.info("Survey report headers fetched successfully. Total: {}", headers.size());
//        return ApiResponse.success("Survey report headers fetched successfully", headers);
//    }
}