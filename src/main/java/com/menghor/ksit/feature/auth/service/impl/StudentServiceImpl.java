package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.dto.relationship.StudentParentDto;
import com.menghor.ksit.feature.auth.dto.relationship.StudentSiblingDto;
import com.menghor.ksit.feature.auth.dto.relationship.StudentStudiesHistoryDto;
import com.menghor.ksit.feature.auth.dto.request.StudentBatchCreateRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StudentCreateRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StudentUpdateRequestDto;
import com.menghor.ksit.feature.auth.dto.filter.StudentUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StudentUserListResponseDto;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private final StudentIdentifierGenerator identifierGenerator;

    @Override
    @Transactional
    public StudentUserResponseDto registerStudent(StudentCreateRequestDto requestDto) {
        log.info("Registering new student with email: {}", requestDto.getEmail());

        // Generate student identifier based on class code
        String identifyNumber = identifierGenerator.generateStudentIdentifier(requestDto.getClassId());
        log.info("Generated identifyNumber: {}", identifyNumber);

        // Set username to identifyNumber if not provided
        String username = requestDto.getUsername();
        if (username == null || username.isEmpty()) {
            username = identifyNumber;
            log.info("Using identifyNumber as username: {}", username);
        }

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            log.warn("Attempt to register with duplicate username: {}", username);
            throw new DuplicateNameException("Username '" + username + "' is already in use. Please try a different username.");
        }

        // Check if identifyNumber already exists
        if (userRepository.existsByIdentifyNumber(identifyNumber)) {
            log.warn("Attempt to register with duplicate identifyNumber: {}", identifyNumber);
            throw new DuplicateNameException("Student ID number '" + identifyNumber + "' is already in use. Please contact an administrator.");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            log.warn("Attempt to register with duplicate username: {}", username);
            throw new DuplicateNameException("Username is already in use");
        }

        UserEntity student = new UserEntity();

        // Set common fields
        student.setUsername(username);
        student.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        student.setStatus(requestDto.getStatus() != null ? requestDto.getStatus() : Status.ACTIVE);
        student.setEmail(requestDto.getEmail());
        student.setIdentifyNumber(identifyNumber);

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
        ClassEntity classEntity = classRepository.findById(requestDto.getClassId())
                .orElseThrow(() -> new BadRequestException("Class not found with ID: " + requestDto.getClassId()));
        student.setClasses(classEntity);

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
        log.info("Student registered successfully with ID: {}, username: {}, identifyNumber: {}",
                savedStudent.getId(), savedStudent.getUsername(), savedStudent.getIdentifyNumber());

        return studentMapper.toStudentUserDto(savedStudent);
    }

    @Override
    @Transactional
    public List<StudentResponseDto> batchRegisterStudents(StudentBatchCreateRequestDto batchRequest) {
        log.info("Batch registering {} students for class ID: {}", batchRequest.getQuantity(), batchRequest.getClassId());

        // Check if class exists
        ClassEntity classEntity = classRepository.findById(batchRequest.getClassId())
                .orElseThrow(() -> new BadRequestException("Class not found with ID: " + batchRequest.getClassId()));

        // Get the STUDENT role
        Role studentRole = roleRepository.findByName(RoleEnum.STUDENT)
                .orElseThrow(() -> new BadRequestException("Student role not found"));

        // Create students
        return IntStream.range(0, batchRequest.getQuantity())
                .mapToObj(i -> {
                    try {
                        // Generate identifier
                        String identifyNumber = identifierGenerator.generateStudentIdentifier(batchRequest.getClassId());

                        // Check if identifyNumber already exists
                        if (userRepository.existsByIdentifyNumber(identifyNumber)) {
                            log.warn("Skipping batch student creation - duplicate identifyNumber: {}", identifyNumber);
                            throw new DuplicateNameException("Student ID '" + identifyNumber + "' is already in use. Skipping this student creation.");
                        }

                        // Generate password
                        String plainTextPassword = identifierGenerator.generateRandomPassword();

                        // Create student entity
                        UserEntity student = new UserEntity();

                        // Set username from identifyNumber
                        student.setUsername(identifyNumber);
                        student.setPassword(passwordEncoder.encode(plainTextPassword));
                        student.setStatus(batchRequest.getStatus() != null ? batchRequest.getStatus() : Status.ACTIVE);
                        student.setIdentifyNumber(identifyNumber);

                        // Set class
                        student.setClasses(classEntity);

                        // Set role
                        student.setRoles(Collections.singletonList(studentRole));

                        // Save student
                        UserEntity savedStudent = userRepository.save(student);

                        log.info("Batch created student #{} with ID: {}, username: {}, identifyNumber: {}",
                                (i + 1), savedStudent.getId(), savedStudent.getUsername(),
                                savedStudent.getIdentifyNumber());

                        return studentMapper.toStudentBatchDto(savedStudent,plainTextPassword);
                    } catch (Exception e) {
                        log.error("Error creating batch student #{}: {}", (i + 1), e.getMessage());
                        throw new BadRequestException("Error creating batch student #" + (i + 1) + ": " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public StudentUserAllResponseDto getAllStudentUsers(StudentUserFilterRequestDto filterDto) {
        log.info("Searching student users with filter: {}", filterDto);

        // Validate pagination parameters
        PaginationUtils.validatePagination(filterDto.getPageNo(), filterDto.getPageSize());

        // Add sorting by creation date in descending order (newest to oldest)
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Build specification for filtering
        Specification<UserEntity> specification = UserSpecification.createStudentSpecification(filterDto);

        // Fetch paginated users
        Page<UserEntity> userPage = userRepository.findAll(specification, pageable);

        // Convert to response DTOs
        List<StudentUserListResponseDto> userDtos = studentMapper.toStudentUserDtoList(userPage.getContent());

        // Create and return paginated response
        return studentMapper.toStudentPageResponse(userDtos, userPage);
    }

    @Override
    public StudentUserResponseDto getStudentUserById(Long id) {
        log.info("Fetching student user with ID: {}", id);

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

        // If changing email, check if it would conflict with any existing username
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(student.getEmail())) {
            if (userRepository.existsByUsername(updateDto.getEmail())) {
                log.warn("Cannot update - email would conflict with existing username: {}", updateDto.getEmail());
                throw new DuplicateNameException("Email '" + updateDto.getEmail() + "' is already registered as a username. Please use a different email.");
            }
        }

        // Update personal info
        if (updateDto.getEmail() != null) student.setEmail(updateDto.getEmail());
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