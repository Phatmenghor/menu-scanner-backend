package com.menghor.ksit.feature.survey.dto.filter;

import lombok.Data;

import java.util.List;

@Data
public class SurveyReportHeaderFilterDto {
    // Only headers to hide - much simpler approach
    private List<String> hiddenHeaders;
}