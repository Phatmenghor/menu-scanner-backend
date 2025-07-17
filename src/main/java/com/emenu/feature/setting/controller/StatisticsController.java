package com.emenu.feature.setting.controller;

import com.emenu.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.setting.dto.response.StatisticsResponseDto;
import com.menghor.ksit.feature.setting.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Get overall system statistics for active entities
     * Only accessible by ADMIN and DEVELOPER roles
     */
    @GetMapping("/overview")
    public ApiResponse<StatisticsResponseDto> getOverallStatistics() {
        log.info("REST request to get overall system statistics");
        
        StatisticsResponseDto statistics = statisticsService.getOverallStatistics();
        
        log.info("Overall statistics retrieved successfully");
        return new ApiResponse<>(
                "success",
                "Statistics retrieved successfully",
                statistics
        );
    }
}