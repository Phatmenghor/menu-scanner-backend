package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.feature.auth.dto.resposne.StudentUserAllResponseDto;
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
           .placeOfBirth(user.getPlaceOfBirth());
        
        // Map student-specific fields
        dto.memberSiblings(user.getMemberSiblings())
           .numberOfSiblings(user.getNumberOfSiblings());
        
        // Map class if available
        if (user.getClasses() != null) {
            dto.studentClass(classMapper.toResponseDto(user.getClasses()));
        }
        
        // Map related lists
        dto.studentStudiesHistory(user.getStudentStudiesHistory())
           .studentParent(user.getStudentParent())
           .studentSibling(user.getStudentSibling());
        
        // Map audit info
        dto.createdAt(user.getCreatedAt())
           .updatedAt(user.getUpdatedAt());
        
        return dto.build();
    }

    @Override
    public List<StudentUserResponseDto> toStudentUserDtoList(List<UserEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        
        return entities.stream()
               .map(this::toStudentUserDto)
               .collect(Collectors.toList());
    }

    @Override
    public StudentUserAllResponseDto toStudentPageResponse(List<StudentUserResponseDto> content, Page<UserEntity> page) {
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