package com.menghor.ksit.feature.school.service;

import com.menghor.ksit.feature.school.dto.response.TranscriptResponseDto;

public interface TranscriptService {
    TranscriptResponseDto getMyCompleteTranscript();

    TranscriptResponseDto getStudentCompleteTranscript(Long studentId);
}