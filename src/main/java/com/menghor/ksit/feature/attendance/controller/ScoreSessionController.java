package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.attendance.dto.request.BatchUpdateScoresRequestDto;
import com.menghor.ksit.feature.attendance.dto.request.CalculateAttendanceScoresRequestDto;
import com.menghor.ksit.feature.attendance.dto.request.ScoreSessionRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreSessionResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.ScoreSessionUpdateDto;
import com.menghor.ksit.feature.attendance.service.ScoreSessionService;
import com.menghor.ksit.utils.database.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/score-sessions")
@RequiredArgsConstructor
@Slf4j
public class ScoreSessionController {
    
    private final ScoreSessionService scoreSessionService;
    private final SecurityUtils securityUtils;
    
    @PostMapping("/initialize")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'TEACHER')")
    public ApiResponse<ScoreSessionResponseDto> initializeScoreSession(@Valid @RequestBody ScoreSessionRequestDto requestDto) {
        log.info("REST request to initialize score session: {}", requestDto);
        ScoreSessionResponseDto responseDto = scoreSessionService.initializeScoreSession(requestDto);
        return new ApiResponse<>(
                "success",
                "Score session initialized successfully",
                responseDto
        );
    }
    
    @GetMapping("/{id}")
    public ApiResponse<ScoreSessionResponseDto> getScoreSessionById(@PathVariable Long id) {
        log.info("REST request to get score session by ID: {}", id);
        ScoreSessionResponseDto responseDto = scoreSessionService.getScoreSessionById(id);
        return new ApiResponse<>(
                "success",
                "Score session retrieved successfully",
                responseDto
        );
    }
    
    @GetMapping("/schedule/{scheduleId}")
    public ApiResponse<ScoreSessionResponseDto> getScoreSessionByScheduleId(@PathVariable Long scheduleId) {
        log.info("REST request to get score session by schedule ID: {}", scheduleId);
        ScoreSessionResponseDto responseDto = scoreSessionService.getScoreSessionByScheduleId(scheduleId);
        return new ApiResponse<>(
                "success",
                "Score session retrieved successfully",
                responseDto
        );
    }
    
    @PostMapping("/update-scores")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'TEACHER')")
    public ApiResponse<ScoreSessionResponseDto> batchUpdateStudentScores(
            @Valid @RequestBody BatchUpdateScoresRequestDto requestDto) {
        log.info("REST request to batch update student scores: {}", requestDto);
        ScoreSessionResponseDto responseDto = scoreSessionService.batchUpdateStudentScores(requestDto);
        return new ApiResponse<>(
                "success",
                "Student scores updated successfully",
                responseDto
        );
    }
    
    @PutMapping("/update")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'TEACHER', 'STAFF')")
    public ApiResponse<ScoreSessionResponseDto> updateScoreSession(
            @Valid @RequestBody ScoreSessionUpdateDto updateDto) {
        log.info("REST request to update score session: {}", updateDto);
        ScoreSessionResponseDto responseDto = scoreSessionService.updateScoreSession(updateDto);
        return new ApiResponse<>(
                "success",
                "Score session updated successfully",
                responseDto
        );
    }
    
    @PostMapping("/submit/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'TEACHER')")
    public ApiResponse<ScoreSessionResponseDto> submitForReview(
            @PathVariable Long id, @RequestParam(required = false) String comments) {
        log.info("REST request to submit score session for review: {}", id);
        ScoreSessionResponseDto responseDto = scoreSessionService.submitForReview(id, comments);
        return new ApiResponse<>(
                "success",
                "Score session submitted for review successfully",
                responseDto
        );
    }
    
    @PostMapping("/review/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<ScoreSessionResponseDto> reviewScoreSession(
            @PathVariable Long id, 
            @RequestParam String status,
            @RequestParam(required = false) String comments) {
        log.info("REST request to review score session: {}, status: {}", id, status);
        ScoreSessionResponseDto responseDto = scoreSessionService.reviewScoreSession(id, status, comments);
        return new ApiResponse<>(
                "success",
                "Score session reviewed successfully",
                responseDto
        );
    }
    
    @PostMapping("/calculate-attendance")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'TEACHER')")
    public ApiResponse<String> calculateAttendanceScores(
            @Valid @RequestBody CalculateAttendanceScoresRequestDto requestDto) {
        log.info("REST request to calculate attendance scores for session ID: {}", requestDto.getScoreSessionId());
        scoreSessionService.calculateAttendanceScores(requestDto);
        return new ApiResponse<>(
                "success",
                "Attendance scores calculated successfully",
                "Attendance scores have been calculated for all students in this session"
        );
    }
    
    @GetMapping("/teacher")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'TEACHER')")
    public ApiResponse<List<ScoreSessionResponseDto>> getScoreSessionsByTeacher() {
        Long teacherId = securityUtils.getCurrentUser().getId();
        log.info("REST request to get score sessions for teacher ID: {}", teacherId);
        List<ScoreSessionResponseDto> responseDtos = scoreSessionService.getScoreSessionsByTeacherId(teacherId);
        return new ApiResponse<>(
                "success",
                "Score sessions retrieved successfully",
                responseDtos
        );
    }
    
    @GetMapping("/for-review")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<List<ScoreSessionResponseDto>> getScoreSessionsForReview() {
        log.info("REST request to get score sessions for review");
        List<ScoreSessionResponseDto> responseDtos = scoreSessionService.getScoreSessionsForReview();
        return new ApiResponse<>(
                "success",
                "Score sessions for review retrieved successfully",
                responseDtos
        );
    }
    
    @PostMapping("/process-new-student/{studentId}/{classId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER')")
    public ApiResponse<String> processNewlyAddedStudent(
            @PathVariable Long studentId, @PathVariable Long classId) {
        log.info("REST request to process newly added student ID: {} for class ID: {}", studentId, classId);
        scoreSessionService.processNewlyAddedStudent(studentId, classId);
        return new ApiResponse<>(
                "success",
                "Newly added student processed successfully",
                "Student has been added to all existing score sessions for the class"
        );
    }
}