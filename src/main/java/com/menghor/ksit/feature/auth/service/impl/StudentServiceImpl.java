package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.dto.relationship.StudentParentDto;
import com.menghor.ksit.feature.auth.dto.relationship.StudentSiblingDto;
import com.menghor.ksit.feature.auth.dto.relationship.StudentStudiesHistoryDto;
import com.menghor.ksit.feature.auth.dto.request.StudentCreateRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StudentUpdateRequestDto;
import com.menghor.ksit.feature.auth.dto.filter.StudentUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserResponseDto;
import com.menghor.ksit.feature.auth.mapper.StaffMapper;
import com.menghor.ksit.feature.auth.mapper.StudentMapper;
import com.menghor.ksit.feature.auth.models.*;
import com.menghor.ksit.feature.auth.repository.RoleRepository;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.auth.repository.UserSpecification;
import com.menghor.ksit.feature.auth.service.StudentService;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.repository.ClassRepository;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClassRepository classRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentMapper studentMapper;
    private final StaffMapper staffMapper;

    @Override
    @Transactional
    public StudentUserResponseDto registerStudent(StudentCreateRequestDto requestDto) {
        log.info("Registering new student with email: {}", requestDto.getEmail());
        
        // Check if username already exists
        if (userRepository.existsByUsername(requestDto.getEmail())) {
            log.warn("Attempt to register with duplicate email: {}", requestDto.getEmail());
            throw new DuplicateNameException("Email is already in use");
        }

        UserEntity student = new UserEntity();

        // Set common fields
        student.setUsername(requestDto.getEmail());
        student.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        student.setStatus(requestDto.getStatus() != null ? requestDto.getStatus() : Status.ACTIVE);
        student.setEmail(requestDto.getEmail());

        // Set personal information
        student.setKhmerFirstName(requestDto.getKhmerFirstName());
        student.setKhmerLastName(requestDto.getKhmerLastName());
        student.setEnglishFirstName(requestDto.getEnglishFirstName());
        student.setEnglishLastName(requestDto.getEnglishLastName());
        student.setGender(requestDto.getGender());
        student.setDateOfBirth(requestDto.getDateOfBirth());
        student.setPhoneNumber(requestDto.getPhoneNumber());
        student.setCurrentAddress(requestDto.getCurrentAddress());
        student.setNationality(requestDto.getNationality());
        student.setEthnicity(requestDto.getEthnicity());
        student.setPlaceOfBirth(requestDto.getPlaceOfBirth());

        // Set student-specific fields
        student.setMemberSiblings(requestDto.getMemberSiblings());
        student.setNumberOfSiblings(requestDto.getNumberOfSiblings());

        // Assign to class if provided
        if (requestDto.getClassId() != null) {
            ClassEntity classEntity = classRepository.findById(requestDto.getClassId())
                    .orElseThrow(() -> new BadRequestException("Class not found with ID: " + requestDto.getClassId()));
            student.setClasses(classEntity);
        }

        // Set STUDENT role
        Role studentRole = roleRepository.findByName(RoleEnum.STUDENT)
                .orElseThrow(() -> new BadRequestException("Student role not found"));
        student.setRoles(Collections.singletonList(studentRole));
        
        // Handle student studies history
        if (requestDto.getStudentStudiesHistories() != null && !requestDto.getStudentStudiesHistories().isEmpty()) {
            List<StudentStudiesHistoryEntity> historyEntities = new ArrayList<>();
            for (StudentStudiesHistoryDto dto : requestDto.getStudentStudiesHistories()) {
                StudentStudiesHistoryEntity entity = new StudentStudiesHistoryEntity();
                entity.setTypeStudies(dto.getTypeStudies());
                entity.setSchoolName(dto.getSchoolName());
                entity.setLocation(dto.getLocation());
                entity.setFromYear(dto.getFromYear());
                entity.setEndYear(dto.getEndYear());
                entity.setObtainedCertificate(dto.getObtainedCertificate());
                entity.setOverallGrade(dto.getOverallGrade());
                entity.setUser(student);
                historyEntities.add(entity);
            }
            student.setStudentStudiesHistory(historyEntities);
        }
        
        // Handle student parent information
        if (requestDto.getStudentParents() != null && !requestDto.getStudentParents().isEmpty()) {
            List<StudentParentEntity> parentEntities = new ArrayList<>();
            for (StudentParentDto dto : requestDto.getStudentParents()) {
                StudentParentEntity entity = new StudentParentEntity();
                entity.setName(dto.getName());
                entity.setPhone(dto.getPhone());
                entity.setJob(dto.getJob());
                entity.setAddress(dto.getAddress());
                entity.setAge(dto.getAge());
                entity.setParentType(dto.getParentType());
                entity.setUser(student);
                parentEntities.add(entity);
            }
            student.setStudentParent(parentEntities);
        }
        
        // Handle student siblings
        if (requestDto.getStudentSiblings() != null && !requestDto.getStudentSiblings().isEmpty()) {
            List<StudentSiblingEntity> siblingEntities = new ArrayList<>();
            for (StudentSiblingDto dto : requestDto.getStudentSiblings()) {
                StudentSiblingEntity entity = new StudentSiblingEntity();
                entity.setName(dto.getName());
                entity.setGender(dto.getGender());
                entity.setDateOfBirth(dto.getDateOfBirth());
                entity.setOccupation(dto.getOccupation());
                entity.setUser(student);
                siblingEntities.add(entity);
            }
            student.setStudentSibling(siblingEntities);
        }

        UserEntity savedStudent = userRepository.save(student);
        log.info("Student registered successfully with ID: {}", savedStudent.getId());

        return studentMapper.toStudentUserDto(savedStudent);
    }

    @Override
    public StudentUserAllResponseDto getAllStudentUsers(StudentUserFilterRequestDto filterDto) {
        log.info("Fetching all student users with filter: {}", filterDto);

        // Set default pagination values if null
        if (filterDto.getPageNo() == null) filterDto.setPageNo(1);
        if (filterDto.getPageSize() == null) filterDto.setPageSize(10);

        // Validate pagination parameters
        PaginationUtils.validatePagination(filterDto.getPageNo(), filterDto.getPageSize());

        // Create pageable object with sorting
        int pageNo = filterDto.getPageNo() - 1; // Convert to 0-based
        int pageSize = filterDto.getPageSize();

        // Add sorting by creation date in descending order (newest to oldest)
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Build specification for filtering
        Specification<UserEntity> specification = UserSpecification.createStudentSpecification(filterDto);

        // Fetch paginated users
        Page<UserEntity> userPage = userRepository.findAll(specification, pageable);

        // Convert to response DTOs
        List<StudentUserResponseDto> userDtos = studentMapper.toStudentUserDtoList(userPage.getContent());

        // Create and return paginated response
        return studentMapper.toStudentPageResponse(userDtos, userPage);
    }

    @Override
    public StudentUserResponseDto getStudentUserById(Long id) {
        log.info("Fetching student user by ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException("User not found with ID: " + id);
                });

        // Verify user is a student
        if (!user.isStudent()) {
            throw new BadRequestException("User with ID " + id + " is not a student");
        }

        // Ensure status is set
        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
        }

        return studentMapper.toStudentUserDto(user);
    }

    @Override
    @Transactional
    public StudentUserResponseDto updateStudentUser(Long id, StudentUpdateRequestDto updateDto) {
        log.info("Updating student user with ID: {}", id);

        UserEntity student = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException("User not found with ID: " + id);
                });

        // Verify user is a student
        if (!student.isStudent()) {
            throw new BadRequestException("User with ID " + id + " is not a student");
        }

        // Check if email is changing and if it conflicts
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(student.getUsername()) && 
                userRepository.existsByUsername(updateDto.getEmail())) {
            throw new DuplicateNameException("Email is already in use");
        }

        // Update basic fields if provided
        if (updateDto.getEmail() != null) {
            student.setUsername(updateDto.getEmail());
            student.setEmail(updateDto.getEmail());
        }

        // Update personal information
        if (updateDto.getKhmerFirstName() != null) student.setKhmerFirstName(updateDto.getKhmerFirstName());
        if (updateDto.getKhmerLastName() != null) student.setKhmerLastName(updateDto.getKhmerLastName());
        if (updateDto.getEnglishFirstName() != null) student.setEnglishFirstName(updateDto.getEnglishFirstName());
        if (updateDto.getEnglishLastName() != null) student.setEnglishLastName(updateDto.getEnglishLastName());
        if (updateDto.getGender() != null) student.setGender(updateDto.getGender());
        if (updateDto.getDateOfBirth() != null) student.setDateOfBirth(updateDto.getDateOfBirth());
        if (updateDto.getPhoneNumber() != null) student.setPhoneNumber(updateDto.getPhoneNumber());
        if (updateDto.getCurrentAddress() != null) student.setCurrentAddress(updateDto.getCurrentAddress());
        if (updateDto.getNationality() != null) student.setNationality(updateDto.getNationality());
        if (updateDto.getEthnicity() != null) student.setEthnicity(updateDto.getEthnicity());
        if (updateDto.getPlaceOfBirth() != null) student.setPlaceOfBirth(updateDto.getPlaceOfBirth());

        // Update student-specific fields
        if (updateDto.getMemberSiblings() != null) student.setMemberSiblings(updateDto.getMemberSiblings());
        if (updateDto.getNumberOfSiblings() != null) student.setNumberOfSiblings(updateDto.getNumberOfSiblings());

        // Update class if provided
        if (updateDto.getClassId() != null) {
            ClassEntity classEntity = classRepository.findById(updateDto.getClassId())
                    .orElseThrow(() -> new BadRequestException("Class not found with ID: " + updateDto.getClassId()));
            student.setClasses(classEntity);
        }
        
        // Update status if provided
        if (updateDto.getStatus() != null) {
            student.setStatus(updateDto.getStatus());
        }
        
        // Handle student studies history
        if (updateDto.getStudentStudiesHistories() != null) {
            student.getStudentStudiesHistory().clear();
            
            for (StudentStudiesHistoryDto dto : updateDto.getStudentStudiesHistories()) {
                StudentStudiesHistoryEntity entity;
                
                if (dto.getId() != null) {
                    entity = student.getStudentStudiesHistory().stream()
                            .filter(e -> e.getId().equals(dto.getId()))
                            .findFirst()
                            .orElse(new StudentStudiesHistoryEntity());
                } else {
                    entity = new StudentStudiesHistoryEntity();
                }
                
                entity.setTypeStudies(dto.getTypeStudies());
                entity.setSchoolName(dto.getSchoolName());
                entity.setLocation(dto.getLocation());
                entity.setFromYear(dto.getFromYear());
                entity.setEndYear(dto.getEndYear());
                entity.setObtainedCertificate(dto.getObtainedCertificate());
                entity.setOverallGrade(dto.getOverallGrade());
                entity.setUser(student);
                
                student.getStudentStudiesHistory().add(entity);
            }
        }
        
        // Handle student parent information
        if (updateDto.getStudentParents() != null) {
            student.getStudentParent().clear();
            
            for (StudentParentDto dto : updateDto.getStudentParents()) {
                StudentParentEntity entity;
                
                if (dto.getId() != null) {
                    entity = student.getStudentParent().stream()
                            .filter(e -> e.getId().equals(dto.getId()))
                            .findFirst()
                            .orElse(new StudentParentEntity());
                } else {
                    entity = new StudentParentEntity();
                }
                
                entity.setName(dto.getName());
                entity.setPhone(dto.getPhone());
                entity.setJob(dto.getJob());
                entity.setAddress(dto.getAddress());
                entity.setAge(dto.getAge());
                entity.setParentType(dto.getParentType());
                entity.setUser(student);
                
                student.getStudentParent().add(entity);
            }
        }
        
        // Handle student siblings
        if (updateDto.getStudentSiblings() != null) {
            student.getStudentSibling().clear();
            
            for (StudentSiblingDto dto : updateDto.getStudentSiblings()) {
                StudentSiblingEntity entity;
                
                if (dto.getId() != null) {
                    entity = student.getStudentSibling().stream()
                            .filter(e -> e.getId().equals(dto.getId()))
                            .findFirst()
                            .orElse(new StudentSiblingEntity());
                } else {
                    entity = new StudentSiblingEntity();
                }
                
                entity.setName(dto.getName());
                entity.setGender(dto.getGender());
                entity.setDateOfBirth(dto.getDateOfBirth());
                entity.setOccupation(dto.getOccupation());
                entity.setUser(student);
                
                student.getStudentSibling().add(entity);
            }
        }

        UserEntity updatedStudent = userRepository.save(student);
        log.info("Student user with ID {} updated successfully", id);

        return studentMapper.toStudentUserDto(updatedStudent);
    }

    @Override
    @Transactional
    public StudentUserResponseDto deleteStudentUser(Long id) {
        log.info("Deleting/deactivating student user with ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException("User not found with ID: " + id);
                });

        // Verify user is a student
        if (!user.isStudent()) {
            throw new BadRequestException("User with ID " + id + " is not a student");
        }

        // Instead of hard delete, deactivate the user
        user.setStatus(Status.INACTIVE);
        UserEntity deactivatedUser = userRepository.save(user);

        log.info("Student user with ID {} deactivated successfully", id);
        return studentMapper.toStudentUserDto(deactivatedUser);
    }
}