package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.feature.auth.dto.relationship.*;
import com.menghor.ksit.feature.auth.models.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for relationship entities to their respective DTOs
 * Handles proper mapping without circular references
 */
@Mapper(componentModel = "spring")
@Component
public interface RelationshipMapper {

    // Teacher Professional Rank
    TeachersProfessionalRankDto toTeacherRankDto(TeachersProfessionalRankEntity entity);

    default List<TeachersProfessionalRankDto> toTeacherRankDtoList(List<TeachersProfessionalRankEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(this::toTeacherRankDto)
                .collect(Collectors.toList());
    }

    // Teacher Experience
    TeacherExperienceDto toTeacherExperienceDto(TeacherExperienceEntity entity);

    default List<TeacherExperienceDto> toTeacherExperienceDtoList(List<TeacherExperienceEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(this::toTeacherExperienceDto)
                .collect(Collectors.toList());
    }

    // Teacher Praise or Criticism
    TeacherPraiseOrCriticismDto toTeacherPraiseDto(TeacherPraiseOrCriticismEntity entity);

    default List<TeacherPraiseOrCriticismDto> toTeacherPraiseDtoList(List<TeacherPraiseOrCriticismEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(this::toTeacherPraiseDto)
                .collect(Collectors.toList());
    }

    // Teacher Education
    TeacherEducationDto toTeacherEducationDto(TeacherEducationEntity entity);

    default List<TeacherEducationDto> toTeacherEducationDtoList(List<TeacherEducationEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(this::toTeacherEducationDto)
                .collect(Collectors.toList());
    }

    // Teacher Vocational
    TeacherVocationalDto toTeacherVocationalDto(TeacherVocationalEntity entity);

    default List<TeacherVocationalDto> toTeacherVocationalDtoList(List<TeacherVocationalEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(this::toTeacherVocationalDto)
                .collect(Collectors.toList());
    }

    // Teacher Short Course
    TeacherShortCourseDto toTeacherShortCourseDto(TeacherShortCourseEntity entity);

    default List<TeacherShortCourseDto> toTeacherShortCourseDtoList(List<TeacherShortCourseEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(this::toTeacherShortCourseDto)
                .collect(Collectors.toList());
    }

    // Teacher Language
    TeacherLanguageDto toTeacherLanguageDto(TeacherLanguageEntity entity);

    default List<TeacherLanguageDto> toTeacherLanguageDtoList(List<TeacherLanguageEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(this::toTeacherLanguageDto)
                .collect(Collectors.toList());
    }

    // Teacher Family
    TeacherFamilyDto toTeacherFamilyDto(TeacherFamilyEntity entity);

    default List<TeacherFamilyDto> toTeacherFamilyDtoList(List<TeacherFamilyEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(this::toTeacherFamilyDto)
                .collect(Collectors.toList());
    }

    // Student Studies History
    StudentStudiesHistoryDto toStudentStudiesHistoryDto(StudentStudiesHistoryEntity entity);

    default List<StudentStudiesHistoryDto> toStudentStudiesHistoryDtoList(List<StudentStudiesHistoryEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(this::toStudentStudiesHistoryDto)
                .collect(Collectors.toList());
    }

    // Student Parent
    StudentParentDto toStudentParentDto(StudentParentEntity entity);

    default List<StudentParentDto> toStudentParentDtoList(List<StudentParentEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(this::toStudentParentDto)
                .collect(Collectors.toList());
    }

    // Student Sibling
    StudentSiblingDto toStudentSiblingDto(StudentSiblingEntity entity);

    default List<StudentSiblingDto> toStudentSiblingDtoList(List<StudentSiblingEntity> entities) {
        if (entities == null) {
            return java.util.Collections.emptyList();
        }
        return entities.stream()
                .map(this::toStudentSiblingDto)
                .collect(Collectors.toList());
    }
}