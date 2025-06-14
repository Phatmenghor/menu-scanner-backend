package com.menghor.ksit.feature.survey.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SurveyReportResponseDto {
    // Dynamic headers based on survey questions
    private List<SurveyReportHeaderDto> headers;
    
    // Data rows
    private List<SurveyReportRowDto> rows;
    
    // Pagination info (only for web preview)
    private Integer pageNo;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private Boolean last;
}