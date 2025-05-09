package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserResponseDto;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StaffMapperImpl implements StaffMapper {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private RelationshipMapper relationshipMapper;

    @Override
    public StaffUserResponseDto toStaffUserDto(UserEntity user) {
        if (user == null) {
            return null;
        }

        StaffUserResponseDto.StaffUserResponseDtoBuilder dto = StaffUserResponseDto.builder();

        // Map basic user properties
        dto.id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus());

        // Map roles
        if (user.getRoles() != null) {
            List<RoleEnum> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            dto.roles(roles);
        }

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

        // Map staff-specific fields
        dto.staffId(user.getStaffId())
                .nationalId(user.getNationalId())
                .identifyNumber(user.getIdentifyNumber())
                .startWorkDate(user.getStartWorkDate())
                .currentPositionDate(user.getCurrentPositionDate())
                .employeeWork(user.getEmployeeWork())
                .disability(user.getDisability())
                .payrollAccountNumber(user.getPayrollAccountNumber())
                .cppMembershipNumber(user.getCppMembershipNumber())
                .province(user.getProvince())
                .district(user.getDistrict())
                .commune(user.getCommune())
                .village(user.getVillage())
                .officeName(user.getOfficeName())
                .currentPosition(user.getCurrentPosition())
                .decreeFinal(user.getDecreeFinal())
                .rankAndClass(user.getRankAndClass());


        // Map work history and family information
        dto.workHistory(user.getWorkHistory())
                .maritalStatus(user.getMaritalStatus())
                .mustBe(user.getMustBe())
                .affiliatedProfession(user.getAffiliatedProfession())
                .federationName(user.getFederationName())
                .affiliatedOrganization(user.getAffiliatedOrganization())
                .federationEstablishmentDate(user.getFederationEstablishmentDate())
                .wivesSalary(user.getWivesSalary());

        // Use new relationship mapper to convert related entities without circular references
        if (user.getTeachersProfessionalRank() != null) {
            dto.teachersProfessionalRank(relationshipMapper.toTeacherRankDtoList(user.getTeachersProfessionalRank()));
        }

        if (user.getTeacherExperience() != null) {
            dto.teacherExperience(relationshipMapper.toTeacherExperienceDtoList(user.getTeacherExperience()));
        }

        if (user.getTeacherPraiseOrCriticism() != null) {
            dto.teacherPraiseOrCriticism(relationshipMapper.toTeacherPraiseDtoList(user.getTeacherPraiseOrCriticism()));
        }

        if (user.getTeacherEducation() != null) {
            dto.teacherEducation(relationshipMapper.toTeacherEducationDtoList(user.getTeacherEducation()));
        }

        if (user.getTeacherVocational() != null) {
            dto.teacherVocational(relationshipMapper.toTeacherVocationalDtoList(user.getTeacherVocational()));
        }

        if (user.getTeacherShortCourse() != null) {
            dto.teacherShortCourse(relationshipMapper.toTeacherShortCourseDtoList(user.getTeacherShortCourse()));
        }

        if (user.getTeacherLanguage() != null) {
            dto.teacherLanguage(relationshipMapper.toTeacherLanguageDtoList(user.getTeacherLanguage()));
        }

        if (user.getTeacherFamily() != null) {
            dto.teacherFamily(relationshipMapper.toTeacherFamilyDtoList(user.getTeacherFamily()));
        }

        // Map audit info
        dto.createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        return dto.build();
    }

    @Override
    public List<StaffUserResponseDto> toStaffUserDtoList(List<UserEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(this::toStaffUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public StaffUserAllResponseDto toStaffPageResponse(List<StaffUserResponseDto> content, Page<UserEntity> page) {
        StaffUserAllResponseDto responseDto = new StaffUserAllResponseDto();
        responseDto.setContent(content);
        responseDto.setPageNo(page.getNumber() + 1);
        responseDto.setPageSize(page.getSize());
        responseDto.setTotalElements(page.getTotalElements());
        responseDto.setTotalPages(page.getTotalPages());
        responseDto.setLast(page.isLast());
        return responseDto;
    }
}