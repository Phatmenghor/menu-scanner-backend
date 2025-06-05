package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.attendance.dto.request.ScoreConfigurationRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreConfigurationResponseDto;
import com.menghor.ksit.feature.attendance.service.ScoreConfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/score-configuration")
@RequiredArgsConstructor
@Slf4j
public class ScoreConfigurationController {

    private final ScoreConfigurationService scoreConfigService;

    @PostMapping
    public ApiResponse<ScoreConfigurationResponseDto> createOrUpdateScoreConfiguration(
            @Valid @RequestBody ScoreConfigurationRequestDto requestDto) {

        log.info("REST request to create or update score configuration");

        ScoreConfigurationResponseDto response = scoreConfigService.createOrUpdateScoreConfiguration(requestDto);

        return new ApiResponse<>(
                "success",
                "Score configuration saved successfully",
                response
        );
    }

    @GetMapping
    public ApiResponse<ScoreConfigurationResponseDto> getScoreConfiguration() {

        log.info("REST request to get score configuration");

        ScoreConfigurationResponseDto response = scoreConfigService.getScoreConfiguration();

        return new ApiResponse<>(
                "success",
                "Score configuration retrieved successfully",
                response
        );
    }

    @PostMapping("/validate")
    public ApiResponse<Boolean> validatePercentageTotal(@Valid @RequestBody ScoreConfigurationRequestDto requestDto) {

        log.info("REST request to validate score percentage total");

        boolean isValid = scoreConfigService.validatePercentageTotal(requestDto);

        return new ApiResponse<>(
                "success",
                isValid ? "Score percentages are valid (total = 100%)" :
                        "Score percentages are invalid (total = " + requestDto.getTotalPercentage() + "%)",
                isValid
        );
    }
}