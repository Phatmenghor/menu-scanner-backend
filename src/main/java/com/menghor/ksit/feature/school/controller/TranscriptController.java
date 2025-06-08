package com.menghor.ksit.feature.school.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.school.dto.response.TranscriptResponseDto;
import com.menghor.ksit.feature.school.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transcript")
@RequiredArgsConstructor
@Slf4j
public class TranscriptController {

    private final TranscriptService transcriptService;

    @GetMapping("/my-transcript")
    public ApiResponse<TranscriptResponseDto> getMyTranscript() {
        log.info("Getting complete transcript for current student");

        TranscriptResponseDto transcript = transcriptService.getMyCompleteTranscript();

        log.info("Transcript generated successfully for student ID: {}", transcript.getStudentId());
        return new ApiResponse<>(
                "success",
                "Your complete transcript retrieved successfully",
                transcript
        );
    }

    @GetMapping("/student/{studentId}")
    public ApiResponse<TranscriptResponseDto> getStudentTranscript(@PathVariable Long studentId) {
        log.info("Getting complete transcript for student ID: {}", studentId);

        TranscriptResponseDto transcript = transcriptService.getStudentCompleteTranscript(studentId);

        log.info("Transcript generated successfully for student ID: {}", studentId);
        return new ApiResponse<>(
                "success",
                "Student transcript retrieved successfully",
                transcript
        );
    }
}