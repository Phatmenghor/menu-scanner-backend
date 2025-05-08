package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.constants.ErrorMessages;
import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.dto.request.EnhancedUserUpdateDto;
import com.menghor.ksit.feature.auth.dto.request.StaffUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StudentUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.request.UserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.*;
import com.menghor.ksit.feature.auth.mapper.UserMapper;
import com.menghor.ksit.feature.auth.models.Role;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.RoleRepository;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.auth.repository.UserSpecification;
import com.menghor.ksit.feature.auth.service.UserService;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.repository.ClassRepository;
import com.menghor.ksit.feature.master.repository.DepartmentRepository;
import com.menghor.ksit.utils.database.SecurityUtils;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClassRepository classRepository;
    private final DepartmentRepository departmentRepository;
    private final SecurityUtils securityUtils;
    private final UserMapper userMapper;

    @Override
    public UserAllResponseDto getAllUsers(UserFilterRequestDto filterDto) {
        log.info("Fetching all users with filter: {}", filterDto);

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
        Specification<UserEntity> specification = UserSpecification.createAdvancedSpecification(
                filterDto.getSearch(),
                filterDto.getStatus(),
                filterDto.getRoles()
        );

        // Fetch paginated users
        Page<UserEntity> userPage = userRepository.findAll(specification, pageable);

        // Convert to response DTOs
        List<UserDetailsResponseDto> userDtos = userMapper.toEnhancedDtoList(userPage.getContent());

        // Create and return paginated response
        return userMapper.toPageResponse(userDtos, userPage);
    }

    @Override
    public StaffUserAllResponseDto getAllStaffUsers(StaffUserFilterRequestDto filterDto) {
        log.info("Fetching all staff users with filter: {}", filterDto);

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
        Specification<UserEntity> specification = UserSpecification.createStaffSpecification(filterDto);

        // Fetch paginated users
        Page<UserEntity> userPage = userRepository.findAll(specification, pageable);

        // Convert to response DTOs
        List<StaffUserResponseDto> userDtos = userMapper.toStaffUserDtoList(userPage.getContent());

        // Create and return paginated response
        return userMapper.toStaffPageResponse(userDtos, userPage);
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
        List<StudentUserResponseDto> userDtos = userMapper.toStudentUserDtoList(userPage.getContent());

        // Create and return paginated response
        return userMapper.toStudentPageResponse(userDtos, userPage);
    }

    @Override
    public UserDetailsResponseDto getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id));
                });

        // Ensure status is set
        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
        }

        return userMapper.toEnhancedDto(user);
    }

    @Override
    public StaffUserResponseDto getStaffUserById(Long id) {
        log.info("Fetching staff user by ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id));
                });

        // Verify user is staff type (not a student)
        if (user.isStudent()) {
            throw new BadRequestException("User with ID " + id + " is a student, not a staff user");
        }

        // Ensure status is set
        if (user.getStatus() == null) {
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
        }

        return userMapper.toStaffUserDto(user);
    }

    @Override
    public StudentUserResponseDto getStudentUserById(Long id) {
        log.info("Fetching student user by ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id));
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

        return userMapper.toStudentUserDto(user);
    }

    @Override
    public UserDetailsResponseDto getCurrentUser() {
        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("Fetching current user details for user ID: {}", currentUser.getId());

        if (currentUser.getStatus() == null) {
            currentUser.setStatus(Status.ACTIVE);
            userRepository.save(currentUser);
        }

        return userMapper.toEnhancedDto(currentUser);
    }

    @Override
    @Transactional
    public UserDetailsResponseDto updateUser(Long id, EnhancedUserUpdateDto updateDto) {
        log.info("Updating user with ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id));
                });

        // Update basic fields using mapper
        userMapper.updateUserFromDto(updateDto, user);

        // Handle roles update if provided
        if (updateDto.getRoles() != null && !updateDto.getRoles().isEmpty()) {
            List<Role> roles = new ArrayList<>();
            for (RoleEnum roleEnum : updateDto.getRoles()) {
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> {
                            log.error("Role {} not found", roleEnum);
                            return new NotFoundException("Role not found: " + roleEnum);
                        });
                roles.add(role);
            }
            user.setRoles(roles);
        }

        // Handle class assignment for students if provided
        if (user.isStudent() && updateDto.getClassId() != null) {
            ClassEntity classEntity = classRepository.findById(updateDto.getClassId())
                    .orElseThrow(() -> {
                        log.error("Class with ID {} not found", updateDto.getClassId());
                        return new NotFoundException("Class not found: " + updateDto.getClassId());
                    });
            user.setClasses(classEntity);
        }

        // Handle department assignment for staff if provided
        if (user.isOther() && updateDto.getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findById(updateDto.getDepartmentId())
                    .orElseThrow(() -> {
                        log.error("Department with ID {} not found", updateDto.getDepartmentId());
                        return new NotFoundException("Department not found: " + updateDto.getDepartmentId());
                    });
            user.setDepartment(department);
        }

        // Ensure status is set
        if (user.getStatus() == null) {
            user.setStatus(updateDto.getStatus() != null ? updateDto.getStatus() : Status.ACTIVE);
        }

        UserEntity updatedUser = userRepository.save(user);
        log.info("User with ID {} updated successfully", id);

        return userMapper.toEnhancedDto(updatedUser);
    }

    @Override
    @Transactional
    public UserDetailsResponseDto deleteUser(Long id) {
        log.info("Deleting/deactivating user with ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException(String.format(ErrorMessages.USER_NOT_FOUND, id));
                });

        // Instead of hard delete, deactivate the user
        user.setStatus(Status.INACTIVE);
        UserEntity deactivatedUser = userRepository.save(user);

        log.info("User with ID {} deactivated successfully", id);
        return userMapper.toEnhancedDto(deactivatedUser);
    }

    @Override
    public UserStatisticsResponseDto getUserStatistics() {
        log.info("Generating user statistics");

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getStatus() == Status.ACTIVE)
                .count();

        long totalStudents = userRepository.countByRole(RoleEnum.STUDENT);
        long totalStaff = userRepository.countByRole(RoleEnum.STAFF);
        long totalAdmins = userRepository.countByRole(RoleEnum.ADMIN);

        // Count new users this month (users created after start of current month)
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newUsersThisMonth = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(startOfMonth))
                .count();

        // Count new students this month
        long newStudentsThisMonth = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(startOfMonth))
                .filter(UserEntity::isStudent)
                .count();

        return UserStatisticsResponseDto.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalStudents(totalStudents)
                .totalStaff(totalStaff)
                .totalAdmins(totalAdmins)
                .newUsersThisMonth(newUsersThisMonth)
                .newStudentsThisMonth(newStudentsThisMonth)
                .build();
    }
}