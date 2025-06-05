package com.menghor.ksit.feature.attendance.service;

import com.menghor.ksit.feature.attendance.dto.request.ScoreConfigurationRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreConfigurationResponseDto;

public interface ScoreConfigurationService {

    ScoreConfigurationResponseDto createOrUpdateScoreConfiguration(ScoreConfigurationRequestDto requestDto);

    ScoreConfigurationResponseDto getScoreConfiguration();

    boolean validatePercentageTotal(ScoreConfigurationRequestDto requestDto);

    void initializeDefaultConfiguration();
}