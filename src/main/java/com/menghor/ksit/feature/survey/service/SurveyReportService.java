package com.menghor.ksit.feature.survey.service;

import com.menghor.ksit.feature.survey.dto.filter.SurveyReportFilterDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyReportHeaderDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyReportResponseDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyReportRowDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

import java.util.List;

public interface SurveyReportService {
    
    /**
     * Get survey report with pagination for web preview
     */
    CustomPaginationResponseDto<SurveyReportRowDto> getSurveyReportWithPagination(SurveyReportFilterDto filterDto);
    
    /**
     * Get all survey responses for export (no pagination)
     */
    List<SurveyReportRowDto> getSurveyReportForExport(SurveyReportFilterDto filterDto);
    
    /**
     * Get dynamic headers for the report table
     */
    List<SurveyReportHeaderDto> getSurveyReportHeaders();

    // ===== NEW ACTIVE-ONLY REPORT METHODS (ONLY ACTIVE QUESTIONS) =====

    /**
     * Get survey report with pagination - ONLY ACTIVE QUESTIONS
     * Excludes deleted questions from headers and responses
     */
    CustomPaginationResponseDto<SurveyReportRowDto> getSurveyReportWithPaginationActiveOnly(SurveyReportFilterDto filterDto);

    /**
     * Get survey report for export - ONLY ACTIVE QUESTIONS
     * Excludes deleted questions from the export data
     */
    List<SurveyReportRowDto> getSurveyReportForExportActiveOnly(SurveyReportFilterDto filterDto);

    /**
     * Get survey report headers for ACTIVE QUESTIONS ONLY
     * Excludes deleted questions from the headers
     */
    List<SurveyReportHeaderDto> getSurveyReportHeadersActiveOnly();
}
