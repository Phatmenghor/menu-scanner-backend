package com.menghor.ksit.feature.setting.service;

import com.menghor.ksit.feature.setting.dto.response.StatisticsResponseDto;

public interface StatisticsService {
    
    /**
     * Get overall system statistics for active entities only
     * @return StatisticsResponseDto containing counts of all active entities
     */
    StatisticsResponseDto getOverallStatistics();
}