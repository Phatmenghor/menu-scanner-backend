package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.feature.auth.dto.resposne.StudentResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserListResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserResponseDto;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.mapper.ClassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StudentMapperImpl implements StudentMapper {

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private RelationshipMapper relationshipMapper;

    @Override
    public StudentUserResponseDto toStudentUserDto(UserEntity user) {
        if (user == null) {
            return null;
        }

        StudentUserResponseDto.StudentUserResponseDtoBuilder dto = StudentUserResponseDto.builder();

        // Map basic user properties
        dto.id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus());

        // Map personal info
        dto.khmerFirstName(user.getKhmerFirstName())
                .khmerLastName(user.getKhmerLastName())
                .englishFirstName(user.getEnglishFirstName())
                .englishLastName(user.getEnglishLastName())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .phoneNumber(user.getPhoneNumber())
                .currentAddress(user.getCurrentAddress())
                .nationality(user.getNationality())
                .ethnicity(user.getEthnicity())
                .placeOfBirth(user.getPlaceOfBirth())
                .profileUrl(user.getProfileUrl())
                .identifyNumber(user.getIdentifyNumber());

        // Map student-specific fields
        dto.memberSiblings(user.getMemberSiblings())
                .numberOfSiblings(user.getNumberOfSiblings());

        // Map class if available
        if (user.getClasses() != null) {
            dto.studentClass(classMapper.toResponseDto(user.getClasses()));
        }

        // Use relationship mapper to convert related entities without circular references
        if (user.getStudentStudiesHistory() != null) {
            dto.studentStudiesHistory(relationshipMapper.toStudentStudiesHistoryDtoList(user.getStudentStudiesHistory()));
        }

        if (user.getStudentParent() != null) {
            dto.studentParent(relationshipMapper.toStudentParentDtoList(user.getStudentParent()));
        }

        if (user.getStudentSibling() != null) {
            dto.studentSibling(relationshipMapper.toStudentSiblingDtoList(user.getStudentSibling()));
        }

        return dto.build();
    }

    @Override
    public StudentUserListResponseDto toStudentListUserDto(UserEntity user) {
        if (user == null) {
            return null;
        }

        StudentUserListResponseDto.StudentUserListResponseDtoBuilder dto = StudentUserListResponseDto.builder();

        // Map basic user properties
        dto.id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus());

        // Map class if available
        if (user.getClasses() != null) {
            dto.studentClass(classMapper.toResponseDto(user.getClasses()));
        }

        // Map personal info
        dto.khmerFirstName(user.getKhmerFirstName())
                .khmerLastName(user.getKhmerLastName())
                .englishFirstName(user.getEnglishFirstName())
                .englishLastName(user.getEnglishLastName())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt().toString())
                .profileUrl(user.getProfileUrl())
                .identifyNumber(user.getIdentifyNumber());

        return dto.build();
    }

    @Override
    public StudentResponseDto toStudentBatchDto(UserEntity user, String plainTextPassword) {
        if (user == null) {
            return null;
        }

        StudentResponseDto responseDto = new StudentResponseDto();

        // Set only the required fields
        responseDto.setId(user.getId());
        responseDto.setUsername(user.getUsername());
        responseDto.setIdentifyNumber(user.getIdentifyNumber());
        responseDto.setPassword(plainTextPassword);

        responseDto.setPassword(plainTextPassword);

        // Set class code if class is available
        if (user.getClasses() != null) {
            responseDto.setClassCode(user.getClasses().getCode());
        }

        responseDto.setCreatedAt(user.getCreatedAt());

        return responseDto;
    }


    @Override
    public List<StudentUserListResponseDto> toStudentUserDtoList(List<UserEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(this::toStudentListUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public StudentUserAllResponseDto toStudentPageResponse(List<StudentUserListResponseDto> content, Page<UserEntity> page) {
        StudentUserAllResponseDto responseDto = new StudentUserAllResponseDto();
        responseDto.setContent(content);
        responseDto.setPageNo(page.getNumber() + 1);
        responseDto.setPageSize(page.getSize());
        responseDto.setTotalElements(page.getTotalElements());
        responseDto.setTotalPages(page.getTotalPages());
        responseDto.setLast(page.isLast());
        return responseDto;
    }
}