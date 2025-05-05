package com.menghor.ksit.feature.master.service;

import com.menghor.ksit.feature.master.dto.subject.request.SubjectFilterDto;
import com.menghor.ksit.feature.master.dto.subject.request.SubjectRequestDto;
import com.menghor.ksit.feature.master.dto.subject.response.SubjectResponseDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface SubjectService {

    SubjectResponseDto createSubject(SubjectRequestDto subjectRequestDto);
    SubjectResponseDto getSubjectById(Long id);
    SubjectResponseDto updateSubjectById(SubjectRequestDto subjectRequestDto, Long id);
    SubjectResponseDto deleteSubjectById(Long id);
    CustomPaginationResponseDto<SubjectResponseDto> getAllSubjects(SubjectFilterDto subjectFilterDto);
}
