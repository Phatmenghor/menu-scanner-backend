package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.attendance.dto.filter.ScoreSessionFilterDto;
import com.menghor.ksit.feature.attendance.dto.request.ScoreSessionRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.ScoreSessionUpdateDto;
import com.menghor.ksit.feature.attendance.dto.update.StudentScoreUpdateDto;
import com.menghor.ksit.feature.attendance.service.ScoreSessionService;
import com.menghor.ksit.feature.attendance.service.StudentScoreService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/score")
@RequiredArgsConstructor
@Slf4j
public class ScoreController {
    
    private final ScoreSessionService scoreSessionService;
    private final StudentScoreService studentScoreService;

    @PostMapping("/initialize")
    public ApiResponse<ScoreSessionResponseDto> initializeScoreSession(@Valid @RequestBody ScoreSessionRequestDto requestDto) {
        log.info("REST request to initialize score session: {}", requestDto);
        ScoreSessionResponseDto responseDto = scoreSessionService.initializeScoreSession(requestDto);
        log.info("Score session initialized successfully: {}", responseDto);
        return new ApiResponse<>(
                "success",
                "Score session initialized successfully",
                responseDto
        );
    }
    
    @GetMapping("session/{id}")
    public ApiResponse<ScoreSessionResponseDto> getScoreSessionById(@PathVariable Long id) {
        log.info("REST request to get score session by ID: {}", id);
        ScoreSessionResponseDto responseDto = scoreSessionService.getScoreSessionById(id);
        log.info("Score session retrieved successfully: {}", responseDto);
        return new ApiResponse<>(
                "success",
                "Score session retrieved successfully",
                responseDto
        );
    }

    @PutMapping("/submission-update")
    public ApiResponse<ScoreSessionResponseDto> updateScoreSession(
            @Valid @RequestBody ScoreSessionUpdateDto updateDto) {
        log.info("REST request to update score session: {}", updateDto);
        ScoreSessionResponseDto responseDto = scoreSessionService.updateScoreSession(updateDto);
        log.info("Score session updated successfully: {}", responseDto);
        return new ApiResponse<>(
                "success",
                "Score session updated successfully",
                responseDto
        );
    }

    @PostMapping("/all")
    public ApiResponse<CustomPaginationResponseDto<ScoreSessionResponseDto>> getAllScoreSessions(
            @Valid @RequestBody ScoreSessionFilterDto filterDto) {
        log.info("REST request to get all score sessions with filter: {}", filterDto);
        CustomPaginationResponseDto<ScoreSessionResponseDto> response =
                scoreSessionService.getAllScoreSessions(filterDto);
        log.info("Score sessions retrieved successfully: {}", response);
        return new ApiResponse<>(
                "success",
                "Score sessions retrieved successfully",
                response
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<StudentScoreResponseDto> getStudentScoreById(@PathVariable Long id) {
        log.info("REST request to get student score by ID: {}", id);
        StudentScoreResponseDto responseDto = studentScoreService.getStudentScoreById(id);
        log.info("Student score retrieved successfully: {}", responseDto);
        return new ApiResponse<>(
                "success",
                "Student score retrieved successfully",
                responseDto
        );
    }

    @PutMapping("score-update")
    public ApiResponse<StudentScoreResponseDto> updateStudentScore(
            @Valid @RequestBody StudentScoreUpdateDto updateDto) {
        log.info("REST request to update student score: {}", updateDto);
        StudentScoreResponseDto responseDto = studentScoreService.updateStudentScore(updateDto);
        log.info("Student score updated successfully: {}", responseDto);
        return new ApiResponse<>(
                "success",
                "Student score updated successfully",
                responseDto
        );
    }
}