package com.menghor.ksit.feature.survey.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.survey.dto.filter.SurveyReportFilterDto;
import com.menghor.ksit.feature.survey.dto.filter.SurveyReportHeaderFilterDto;
import com.menghor.ksit.feature.survey.dto.request.SurveyResponseSubmitDto;
import com.menghor.ksit.feature.survey.dto.response.*;
import com.menghor.ksit.feature.survey.dto.update.SurveyUpdateDto;
import com.menghor.ksit.feature.survey.service.SurveyProgressService;
import com.menghor.ksit.feature.survey.service.SurveyReportService;
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
    private final SurveyReportService surveyReportService;
    private final SurveyProgressService surveyProgressService;

    @GetMapping("/main")
    public ApiResponse<SurveyResponseDto> getMainSurvey() {
        log.info("Fetching main survey for admin");
        SurveyResponseDto response = surveyService.getMainSurvey();
        log.info("Main survey fetched successfully");
        return ApiResponse.success("Main survey fetched successfully", response);
    }

    @PutMapping("/main")
    public ApiResponse<SurveyResponseDto> updateMainSurvey(@Valid @RequestBody SurveyUpdateDto updateDto) {
        log.info("Updating main survey with title: {}", updateDto.getTitle());
        SurveyResponseDto response = surveyService.updateMainSurvey(updateDto);
        log.info("Main survey updated successfully");
        return ApiResponse.success("Survey updated successfully", response);
    }

    @DeleteMapping("/section/{sectionId}")
    public ApiResponse<SurveyResponseDto> deleteSurveySection(@PathVariable Long sectionId) {
        log.info("Deleting survey section with ID: {}", sectionId);
        SurveyResponseDto updatedSurvey = surveyService.deleteSurveySectionAndGetUpdatedSurvey(sectionId);
        log.info("Survey section deleted successfully with ID: {}", sectionId);
        return ApiResponse.success("Survey section deleted successfully", updatedSurvey);
    }

    @GetMapping("/schedule/{scheduleId}/students-progress")
    public ApiResponse<ScheduleStudentsProgressDto> getScheduleStudentsProgress(@PathVariable Long scheduleId) {
        log.info("Fetching students survey progress for schedule ID: {}", scheduleId);
        ScheduleStudentsProgressDto response = surveyProgressService.getScheduleStudentsProgress(scheduleId);
        log.info("Students survey progress fetched successfully for schedule: {}. Completion: {}/{} students",
                scheduleId, response.getCompletedSurveys(), response.getTotalStudents());
        return ApiResponse.success("Students survey progress fetched successfully", response);
    }

    @DeleteMapping("/question/{questionId}")
    public ApiResponse<SurveyResponseDto> deleteSurveyQuestion(@PathVariable Long questionId) {
        log.info("Deleting survey question with ID: {}", questionId);
        SurveyResponseDto updatedSurvey = surveyService.deleteSurveyQuestionAndGetUpdatedSurvey(questionId);
        log.info("Survey question deleted successfully with ID: {}", questionId);
        return ApiResponse.success("Survey question deleted successfully", updatedSurvey);
    }

    @PostMapping("/schedule/{scheduleId}/submit")
    public ApiResponse<StudentSurveyResponseDto> submitSurveyResponse(
            @PathVariable Long scheduleId,
            @Valid @RequestBody SurveyResponseSubmitDto submitDto) {
        log.info("Submitting survey response for schedule ID: {}", scheduleId);
        StudentSurveyResponseDto response = surveyService.submitSurveyResponseForSchedule(scheduleId, submitDto);
        log.info("Survey response submitted successfully for schedule: {}", scheduleId);
        return ApiResponse.success("Survey response submitted successfully", response);
    }

    @GetMapping("/schedule/{scheduleId}/my-response")
    public ApiResponse<StudentSurveyResponseDto> getMyResponseForSchedule(@PathVariable Long scheduleId) {
        log.info("Fetching current user's survey response for schedule ID: {}", scheduleId);
        StudentSurveyResponseDto response = surveyService.getMyResponseForSchedule(scheduleId);
        log.info("User's survey response fetched successfully for schedule: {}", scheduleId);
        return ApiResponse.success("Your survey response fetched successfully", response);
    }

    @GetMapping("/response/{responseId}/detail")
    public ApiResponse<SurveyResponseDetailDto> getStudentResponseDetail(@PathVariable Long responseId) {
        log.info("Fetching detailed survey response for ID: {}", responseId);
        SurveyResponseDetailDto response = surveyService.getStudentResponseDetail(responseId);
        log.info("Detailed survey response fetched successfully for ID: {}", responseId);
        return ApiResponse.success("Survey response detail fetched successfully", response);
    }

    @PostMapping("/reports/preview")
    public ApiResponse<CustomPaginationResponseDto<SurveyReportRowDto>> getSurveyReportPreview(
            @Valid @RequestBody SurveyReportFilterDto filterDto) {
        log.info("Fetching survey report preview with filters: {}", filterDto);
        CustomPaginationResponseDto<SurveyReportRowDto> response = surveyReportService.getSurveyReportWithPagination(filterDto);
        log.info("Survey report preview fetched successfully. Total: {}", response.getTotalElements());
        return ApiResponse.success("Survey report preview fetched successfully", response);
    }

    @PostMapping("/reports/export")
    public ApiResponse<List<SurveyReportRowDto>> getSurveyReportForExport(
            @Valid @RequestBody SurveyReportFilterDto filterDto) {
        log.info("Fetching survey report for export with filters: {}", filterDto);
        List<SurveyReportRowDto> response = surveyReportService.getSurveyReportForExport(filterDto);
        log.info("Survey report for export fetched successfully. Total rows: {}", response.size());
        return ApiResponse.success("Survey report for export fetched successfully", response);
    }

    @PostMapping("/reports/headers")
    public ApiResponse<List<SurveyReportHeaderDto>> getSurveyReportHeaders(@Valid @RequestBody SurveyReportHeaderFilterDto headerFilterDto) {
        log.info("Fetching survey report headers");
        List<SurveyReportHeaderDto> headers = surveyReportService.getFilteredSurveyReportHeaders(headerFilterDto);
        log.info("Survey report headers fetched successfully. Total: {}", headers.size());
        return ApiResponse.success("Survey report headers fetched successfully", headers);
    }

    @PostMapping("/reports/active/preview")
    public ApiResponse<CustomPaginationResponseDto<SurveyReportRowDto>> getSurveyReportActivePreview(
            @Valid @RequestBody SurveyReportFilterDto filterDto) {
        log.info("Fetching survey report preview with active questions only, filters: {}", filterDto);
        CustomPaginationResponseDto<SurveyReportRowDto> response = surveyReportService.getSurveyReportWithPaginationActiveOnly(filterDto);
        log.info("Survey report active preview fetched successfully. Total: {}", response.getTotalElements());
        return ApiResponse.success("Survey report active preview fetched successfully", response);
    }

    @PostMapping("/reports/active/export")
    public ApiResponse<List<SurveyReportRowDto>> getSurveyReportActiveForExport(
            @Valid @RequestBody SurveyReportFilterDto filterDto) {
        log.info("Fetching survey report for export with active questions only, filters: {}", filterDto);
        List<SurveyReportRowDto> response = surveyReportService.getSurveyReportForExportActiveOnly(filterDto);
        log.info("Survey report active export fetched successfully. Total rows: {}", response.size());
        return ApiResponse.success("Survey report active export fetched successfully", response);
    }

    @PostMapping("/reports/active/headers")
    public ApiResponse<List<SurveyReportHeaderDto>> getSurveyReportActiveHeaders(@Valid @RequestBody SurveyReportHeaderFilterDto headerFilterDto) {
        log.info("Fetching survey report headers for active questions only");
        List<SurveyReportHeaderDto> headers = surveyReportService.getFilteredSurveyReportHeadersActiveOnly(headerFilterDto);
        log.info("Survey report active headers fetched successfully. Total: {}", headers.size());
        return ApiResponse.success("Survey report active headers fetched successfully", headers);
    }
}