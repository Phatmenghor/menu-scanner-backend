package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.dto.relationship.*;
import com.menghor.ksit.feature.auth.dto.request.StaffCreateRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StaffUpdateRequestDto;
import com.menghor.ksit.feature.auth.dto.filter.StaffUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserListResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserResponseDto;
import com.menghor.ksit.feature.auth.mapper.StaffMapper;
import com.menghor.ksit.feature.auth.models.*;
import com.menghor.ksit.feature.auth.repository.*;
import com.menghor.ksit.feature.auth.service.StaffService;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.repository.DepartmentRepository;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final StaffMapper staffMapper;

    // Required repositories for handling child entities
    private final TeachersProfessionalRankRepository teachersProfessionalRankRepository;
    private final TeacherExperienceRepository teacherExperienceRepository;
    private final TeacherPraiseOrCriticismRepository teacherPraiseOrCriticismRepository;
    private final TeacherEducationRepository teacherEducationRepository;
    private final TeacherVocationalRepository teacherVocationalRepository;
    private final TeacherShortCourseRepository teacherShortCourseRepository;
    private final TeacherLanguageRepository teacherLanguageRepository;
    private final TeacherFamilyRepository teacherFamilyRepository;

    @Override
    @Transactional
    public StaffUserResponseDto registerStaff(StaffCreateRequestDto requestDto) {
        log.info("Registering new staff user with email: {}", requestDto.getEmail());

        // Check if username already exists
        if (userRepository.existsByUsername(requestDto.getEmail())) {
            log.warn("Attempt to register with duplicate email: {}", requestDto.getEmail());
            throw new DuplicateNameException("Email is already in use");
        }

        // Check if identifyNumber already exists (if provided)
        if (StringUtils.hasText(requestDto.getIdentifyNumber()) &&
                userRepository.existsByIdentifyNumber(requestDto.getIdentifyNumber())) {
            log.warn("Attempt to register with duplicate identifyNumber: {}", requestDto.getIdentifyNumber());
            throw new DuplicateNameException("Staff ID number '" + requestDto.getIdentifyNumber() + "' is already in use. Please use a different ID number.");
        }

        UserEntity staff = new UserEntity();

        // Set common fields
        staff.setUsername(requestDto.getUsername());
        staff.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        staff.setStatus(requestDto.getStatus() != null ? requestDto.getStatus() : Status.ACTIVE);
        staff.setEmail(requestDto.getEmail());

        // Set personal information
        staff.setKhmerFirstName(requestDto.getKhmerFirstName());
        staff.setKhmerLastName(requestDto.getKhmerLastName());
        staff.setEnglishFirstName(requestDto.getEnglishFirstName());
        staff.setEnglishLastName(requestDto.getEnglishLastName());
        staff.setGender(requestDto.getGender());
        staff.setDateOfBirth(requestDto.getDateOfBirth());
        staff.setPhoneNumber(requestDto.getPhoneNumber());
        staff.setCurrentAddress(requestDto.getCurrentAddress());
        staff.setNationality(requestDto.getNationality());
        staff.setEthnicity(requestDto.getEthnicity());
        staff.setPlaceOfBirth(requestDto.getPlaceOfBirth());
        staff.setProfileUrl(requestDto.getProfileUrl());

        staff.setTaughtEnglish(requestDto.getTaughtEnglish());
        staff.setThreeLevelClass(requestDto.getThreeLevelClass());
        staff.setReferenceNote(requestDto.getReferenceNote());
        staff.setTechnicalTeamLeader(requestDto.getTechnicalTeamLeader());
        staff.setAssistInTeaching(requestDto.getAssistInTeaching());
        staff.setSerialNumber(requestDto.getSerialNumber());
        staff.setTwoLevelClass(requestDto.getTwoLevelClass());
        staff.setClassResponsibility(requestDto.getClassResponsibility());
        staff.setLastSalaryIncrementDate(requestDto.getLastSalaryIncrementDate());
        staff.setTeachAcrossSchools(requestDto.getTeachAcrossSchools());
        staff.setOvertimeHours(requestDto.getOvertimeHours());
        staff.setIssuedDate(requestDto.getIssuedDate());
        staff.setSuitableClass(requestDto.getSuitableClass());
        staff.setBilingual(requestDto.getBilingual());
        staff.setAcademicYearTaught(requestDto.getAcademicYearTaught());
        staff.setWorkHistory(requestDto.getWorkHistory());


        // Set staff-specific fields
        staff.setStaffId(requestDto.getStaffId());
        staff.setNationalId(requestDto.getNationalId());
        staff.setIdentifyNumber(requestDto.getIdentifyNumber());
        staff.setStartWorkDate(requestDto.getStartWorkDate());
        staff.setCurrentPositionDate(requestDto.getCurrentPositionDate());
        staff.setEmployeeWork(requestDto.getEmployeeWork());
        staff.setDisability(requestDto.getDisability());
        staff.setPayrollAccountNumber(requestDto.getPayrollAccountNumber());
        staff.setCppMembershipNumber(requestDto.getCppMembershipNumber());
        staff.setProvince(requestDto.getProvince());
        staff.setDistrict(requestDto.getDistrict());
        staff.setCommune(requestDto.getCommune());
        staff.setVillage(requestDto.getVillage());
        staff.setOfficeName(requestDto.getOfficeName());
        staff.setCurrentPosition(requestDto.getCurrentPosition());
        staff.setDecreeFinal(requestDto.getDecreeFinal());
        staff.setRankAndClass(requestDto.getRankAndClass());

        // Set work history and family information
        staff.setMaritalStatus(requestDto.getMaritalStatus());
        staff.setMustBe(requestDto.getMustBe());
        staff.setAffiliatedProfession(requestDto.getAffiliatedProfession());
        staff.setFederationName(requestDto.getFederationName());
        staff.setAffiliatedOrganization(requestDto.getAffiliatedOrganization());
        staff.setFederationEstablishmentDate(requestDto.getFederationEstablishmentDate());
        staff.setWivesSalary(requestDto.getWivesSalary());

        // Assign to department if provided
        if (requestDto.getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findById(requestDto.getDepartmentId())
                    .orElseThrow(() -> new BadRequestException("Department not found with ID: " + requestDto.getDepartmentId()));
            staff.setDepartment(department);
        }

        // Set roles
        List<Role> roles = new ArrayList<>();
        if (requestDto.getRoles() != null && !requestDto.getRoles().isEmpty()) {
            for (RoleEnum roleEnum : requestDto.getRoles()) {
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new BadRequestException("Invalid role: " + roleEnum));
                roles.add(role);
            }
        } else {
            Role defaultRole = roleRepository.findByName(RoleEnum.STAFF)
                    .orElseThrow(() -> new BadRequestException("Default role not found"));
            roles.add(defaultRole);
        }
        staff.setRoles(roles);

        // For creation, initialize collections
        staff.setTeachersProfessionalRank(new ArrayList<>());
        staff.setTeacherExperience(new ArrayList<>());
        staff.setTeacherPraiseOrCriticism(new ArrayList<>());
        staff.setTeacherEducation(new ArrayList<>());
        staff.setTeacherVocational(new ArrayList<>());
        staff.setTeacherShortCourse(new ArrayList<>());
        staff.setTeacherLanguage(new ArrayList<>());
        staff.setTeacherFamily(new ArrayList<>());

        // Save the user first to get the ID
        UserEntity savedStaff = userRepository.save(staff);

        // Handle related entity lists
        if (requestDto.getTeachersProfessionalRanks() != null && !requestDto.getTeachersProfessionalRanks().isEmpty()) {
            for (TeachersProfessionalRankDto dto : requestDto.getTeachersProfessionalRanks()) {
                TeachersProfessionalRankEntity entity = new TeachersProfessionalRankEntity();
                entity.setTypeOfProfessionalRank(dto.getTypeOfProfessionalRank());
                entity.setDescription(dto.getDescription());
                entity.setAnnouncementNumber(dto.getAnnouncementNumber());
                entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(savedStaff);
                teachersProfessionalRankRepository.save(entity);
            }
        }

        // Handle teacher experience entries
        if (requestDto.getTeacherExperiences() != null && !requestDto.getTeacherExperiences().isEmpty()) {
            for (TeacherExperienceDto dto : requestDto.getTeacherExperiences()) {
                TeacherExperienceEntity entity = new TeacherExperienceEntity();
                entity.setContinuousEmployment(dto.getContinuousEmployment());
                entity.setWorkPlace(dto.getWorkPlace());
                entity.setStartDate(dto.getStartDate());
                entity.setEndDate(dto.getEndDate());
                entity.setUser(savedStaff);
                teacherExperienceRepository.save(entity);
            }
        }

        // Handle praise/criticism entries
        if (requestDto.getTeacherPraiseOrCriticisms() != null && !requestDto.getTeacherPraiseOrCriticisms().isEmpty()) {
            for (TeacherPraiseOrCriticismDto dto : requestDto.getTeacherPraiseOrCriticisms()) {
                TeacherPraiseOrCriticismEntity entity = new TeacherPraiseOrCriticismEntity();
                entity.setTypePraiseOrCriticism(dto.getTypePraiseOrCriticism());
                entity.setGiveBy(dto.getGiveBy());
                entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(savedStaff);
                teacherPraiseOrCriticismRepository.save(entity);
            }
        }

        // Handle education entries
        if (requestDto.getTeacherEducations() != null && !requestDto.getTeacherEducations().isEmpty()) {
            for (TeacherEducationDto dto : requestDto.getTeacherEducations()) {
                TeacherEducationEntity entity = new TeacherEducationEntity();
                entity.setCulturalLevel(dto.getCulturalLevel());
                entity.setSkillName(dto.getSkillName());
                entity.setDateAccepted(dto.getDateAccepted());
                entity.setCountry(dto.getCountry());
                entity.setUser(savedStaff);
                teacherEducationRepository.save(entity);
            }
        }

        // Handle vocational entries
        if (requestDto.getTeacherVocationals() != null && !requestDto.getTeacherVocationals().isEmpty()) {
            for (TeacherVocationalDto dto : requestDto.getTeacherVocationals()) {
                TeacherVocationalEntity entity = new TeacherVocationalEntity();
                entity.setCulturalLevel(dto.getCulturalLevel());
                entity.setSkillOne(dto.getSkillOne());
                entity.setSkillTwo(dto.getSkillTwo());
                entity.setTrainingSystem(dto.getTrainingSystem());
                entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(savedStaff);
                teacherVocationalRepository.save(entity);
            }
        }

        // Handle short course entries
        if (requestDto.getTeacherShortCourses() != null && !requestDto.getTeacherShortCourses().isEmpty()) {
            for (TeacherShortCourseDto dto : requestDto.getTeacherShortCourses()) {
                TeacherShortCourseEntity entity = new TeacherShortCourseEntity();
                entity.setSkill(dto.getSkill());
                entity.setSkillName(dto.getSkillName());
                entity.setStartDate(dto.getStartDate());
                entity.setEndDate(dto.getEndDate());
                entity.setDuration(dto.getDuration());
                entity.setPreparedBy(dto.getPreparedBy());
                entity.setSupportBy(dto.getSupportBy());
                entity.setUser(savedStaff);
                teacherShortCourseRepository.save(entity);
            }
        }

        // Handle language entries
        if (requestDto.getTeacherLanguages() != null && !requestDto.getTeacherLanguages().isEmpty()) {
            for (TeacherLanguageDto dto : requestDto.getTeacherLanguages()) {
                TeacherLanguageEntity entity = new TeacherLanguageEntity();
                entity.setLanguage(dto.getLanguage());
                entity.setReading(dto.getReading());
                entity.setWriting(dto.getWriting());
                entity.setSpeaking(dto.getSpeaking());
                entity.setUser(savedStaff);
                teacherLanguageRepository.save(entity);
            }
        }

        // Handle family entries
        if (requestDto.getTeacherFamilies() != null && !requestDto.getTeacherFamilies().isEmpty()) {
            for (TeacherFamilyDto dto : requestDto.getTeacherFamilies()) {
                TeacherFamilyEntity entity = new TeacherFamilyEntity();
                entity.setNameChild(dto.getNameChild());
                entity.setGender(dto.getGender());
                entity.setDateOfBirth(dto.getDateOfBirth());
                entity.setWorking(dto.getWorking());
                entity.setUser(savedStaff);
                teacherFamilyRepository.save(entity);
            }
        }

        // Fetch the complete user with all relationships
        UserEntity refreshedStaff = userRepository.findById(savedStaff.getId()).orElseThrow();
        log.info("Staff user registered successfully with ID: {}", refreshedStaff.getId());

        return staffMapper.toStaffUserDto(refreshedStaff);
    }

    @Override
    public StaffUserAllResponseDto getAllStaffUsers(StaffUserFilterRequestDto filterDto) {
        log.info("Fetching all staff users with filter: {}", filterDto);

        // Validate and prepare pagination using PaginationUtils
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Build specification for filtering
        Specification<UserEntity> specification = UserSpecification.createStaffSpecification(filterDto);

        // Fetch paginated users
        Page<UserEntity> userPage = userRepository.findAll(specification, pageable);

        // Convert to response DTOs
        List<StaffUserListResponseDto> userDtos = staffMapper.toStaffUserDtoList(userPage.getContent());

        // Create and return paginated response
        return staffMapper.toStaffPageResponse(userDtos, userPage);
    }

    @Override
    public StaffUserResponseDto getStaffUserById(Long id) {
        log.info("Fetching staff user by ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException("User not found with ID: " + id);
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

        return staffMapper.toStaffUserDto(user);
    }

    @Override
    @Transactional
    public StaffUserResponseDto updateStaffUser(Long id, StaffUpdateRequestDto updateDto) {
        log.info("Updating staff user with ID: {}", id);

        UserEntity staff = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException("User not found with ID: " + id);
                });

        // Verify user is staff type (not a student)
        if (staff.isStudent()) {
            throw new BadRequestException("User with ID " + id + " is a student, not a staff user");
        }

        // Check for email uniqueness if changing email
        if (updateDto.getUsername() != null && !updateDto.getUsername().equals(staff.getUsername()) &&
                userRepository.existsByUsernameAndIdNot(updateDto.getEmail(), id)) {
            log.warn("Attempt to update with duplicate username: {}", updateDto.getEmail());
            throw new DuplicateNameException("Username '" + updateDto.getUsername() + "' is already in use as a username.");
        }

        // Check for email uniqueness if changing email
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(staff.getEmail()) &&
                userRepository.existsByUsername(updateDto.getEmail())) {
            log.warn("Attempt to update with duplicate email: {}", updateDto.getEmail());
            throw new DuplicateNameException("Email '" + updateDto.getEmail() + "' is already in use as a username.");
        }

        // Check for identifyNumber uniqueness if changing identifyNumber
        if (updateDto.getIdentifyNumber() != null && !updateDto.getIdentifyNumber().equals(staff.getIdentifyNumber()) &&
                userRepository.existsByIdentifyNumber(updateDto.getIdentifyNumber())) {
            log.warn("Attempt to update with duplicate identifyNumber: {}", updateDto.getIdentifyNumber());
            throw new DuplicateNameException("Staff ID number '" + updateDto.getIdentifyNumber() + "' is already in use. Please use a different ID number.");
        }

        if(updateDto.getTaughtEnglish() != null) staff.setTaughtEnglish(updateDto.getTaughtEnglish());
        if(updateDto.getThreeLevelClass() != null) staff.setThreeLevelClass(updateDto.getThreeLevelClass());
        if(updateDto.getReferenceNote() != null) staff.setReferenceNote(updateDto.getReferenceNote());
        if(updateDto.getTechnicalTeamLeader() != null) staff.setTechnicalTeamLeader(updateDto.getTechnicalTeamLeader());
        if(updateDto.getAssistInTeaching() != null) staff.setAssistInTeaching(updateDto.getAssistInTeaching());
        if(updateDto.getSerialNumber() != null) staff.setSerialNumber(updateDto.getSerialNumber());
        if(updateDto.getTwoLevelClass() != null) staff.setTwoLevelClass(updateDto.getTwoLevelClass());
        if(updateDto.getClassResponsibility() != null) staff.setClassResponsibility(updateDto.getClassResponsibility());
        if(updateDto.getLastSalaryIncrementDate() != null) staff.setLastSalaryIncrementDate(updateDto.getLastSalaryIncrementDate());
        if(updateDto.getTeachAcrossSchools() != null) staff.setTeachAcrossSchools(updateDto.getTeachAcrossSchools());
        if(updateDto.getOvertimeHours() != null) staff.setOvertimeHours(updateDto.getOvertimeHours());
        if(updateDto.getIssuedDate() != null) staff.setIssuedDate(updateDto.getIssuedDate());
        if(updateDto.getSuitableClass() != null) staff.setSuitableClass(updateDto.getSuitableClass());
        if(updateDto.getBilingual() != null) staff.setBilingual(updateDto.getBilingual());
        if(updateDto.getAcademicYearTaught() != null) staff.setAcademicYearTaught(updateDto.getAcademicYearTaught());
        if(updateDto.getWorkHistory() != null) staff.setWorkHistory(updateDto.getWorkHistory());

        // Update basic fields if provided
        if (updateDto.getUsername() != null) staff.setUsername(updateDto.getUsername());
        if (updateDto.getEmail() != null) staff.setEmail(updateDto.getEmail());
        if (updateDto.getKhmerFirstName() != null) staff.setKhmerFirstName(updateDto.getKhmerFirstName());
        if (updateDto.getKhmerLastName() != null) staff.setKhmerLastName(updateDto.getKhmerLastName());
        if (updateDto.getEnglishFirstName() != null) staff.setEnglishFirstName(updateDto.getEnglishFirstName());
        if (updateDto.getEnglishLastName() != null) staff.setEnglishLastName(updateDto.getEnglishLastName());
        if (updateDto.getGender() != null) staff.setGender(updateDto.getGender());
        if (updateDto.getDateOfBirth() != null) staff.setDateOfBirth(updateDto.getDateOfBirth());
        if (updateDto.getPhoneNumber() != null) staff.setPhoneNumber(updateDto.getPhoneNumber());
        if (updateDto.getCurrentAddress() != null) staff.setCurrentAddress(updateDto.getCurrentAddress());
        if (updateDto.getNationality() != null) staff.setNationality(updateDto.getNationality());
        if (updateDto.getEthnicity() != null) staff.setEthnicity(updateDto.getEthnicity());
        if (updateDto.getPlaceOfBirth() != null) staff.setPlaceOfBirth(updateDto.getPlaceOfBirth());
        if(updateDto.getProfileUrl() != null) staff.setProfileUrl(updateDto.getProfileUrl());

        // Update staff-specific fields
        if (updateDto.getStaffId() != null) staff.setStaffId(updateDto.getStaffId());
        if (updateDto.getNationalId() != null) staff.setNationalId(updateDto.getNationalId());
        if (updateDto.getIdentifyNumber() != null) staff.setIdentifyNumber(updateDto.getIdentifyNumber());
        if (updateDto.getStartWorkDate() != null) staff.setStartWorkDate(updateDto.getStartWorkDate());
        if (updateDto.getCurrentPositionDate() != null) staff.setCurrentPositionDate(updateDto.getCurrentPositionDate());
        if (updateDto.getEmployeeWork() != null) staff.setEmployeeWork(updateDto.getEmployeeWork());
        if (updateDto.getDisability() != null) staff.setDisability(updateDto.getDisability());
        if (updateDto.getPayrollAccountNumber() != null) staff.setPayrollAccountNumber(updateDto.getPayrollAccountNumber());
        if (updateDto.getCppMembershipNumber() != null) staff.setCppMembershipNumber(updateDto.getCppMembershipNumber());
        if (updateDto.getProvince() != null) staff.setProvince(updateDto.getProvince());
        if (updateDto.getDistrict() != null) staff.setDistrict(updateDto.getDistrict());
        if (updateDto.getCommune() != null) staff.setCommune(updateDto.getCommune());
        if (updateDto.getVillage() != null) staff.setVillage(updateDto.getVillage());
        if (updateDto.getOfficeName() != null) staff.setOfficeName(updateDto.getOfficeName());
        if (updateDto.getCurrentPosition() != null) staff.setCurrentPosition(updateDto.getCurrentPosition());
        if (updateDto.getDecreeFinal() != null) staff.setDecreeFinal(updateDto.getDecreeFinal());
        if (updateDto.getRankAndClass() != null) staff.setRankAndClass(updateDto.getRankAndClass());

        // Update work history and family information
        if (updateDto.getMaritalStatus() != null) staff.setMaritalStatus(updateDto.getMaritalStatus());
        if (updateDto.getMustBe() != null) staff.setMustBe(updateDto.getMustBe());
        if (updateDto.getAffiliatedProfession() != null) staff.setAffiliatedProfession(updateDto.getAffiliatedProfession());
        if (updateDto.getFederationName() != null) staff.setFederationName(updateDto.getFederationName());
        if (updateDto.getAffiliatedOrganization() != null) staff.setAffiliatedOrganization(updateDto.getAffiliatedOrganization());
        if (updateDto.getFederationEstablishmentDate() != null) staff.setFederationEstablishmentDate(updateDto.getFederationEstablishmentDate());
        if (updateDto.getWivesSalary() != null) staff.setWivesSalary(updateDto.getWivesSalary());

        // Update department if provided
        if (updateDto.getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findById(updateDto.getDepartmentId())
                    .orElseThrow(() -> new BadRequestException("Department not found with ID: " + updateDto.getDepartmentId()));
            staff.setDepartment(department);
        }

        // Update roles if provided
        if (updateDto.getRoles() != null && !updateDto.getRoles().isEmpty()) {
            List<Role> roles = new ArrayList<>();
            for (RoleEnum roleEnum : updateDto.getRoles()) {
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new BadRequestException("Invalid role: " + roleEnum));
                roles.add(role);
            }
            staff.setRoles(roles);
        }

        // Update status if provided
        if (updateDto.getStatus() != null) {
            staff.setStatus(updateDto.getStatus());
        }

        // Handle related entity lists - Each list needs to handle updates, additions, and removals

        // Save the main entity first to ensure it exists
        UserEntity savedStaff = userRepository.save(staff);

        // TeachersProfessionalRank management
        if (updateDto.getTeachersProfessionalRanks() != null) {
            for (TeachersProfessionalRankDto dto : updateDto.getTeachersProfessionalRanks()) {
                TeachersProfessionalRankEntity entity;

                // If ID exists, try to find existing entity
                if (dto.getId() != null) {
                    // Find entity by ID
                    Optional<TeachersProfessionalRankEntity> existingEntity = teachersProfessionalRankRepository.findById(dto.getId());
                    if (existingEntity.isPresent()) {
                        entity = existingEntity.get();
                    } else {
                        entity = new TeachersProfessionalRankEntity();
                    }
                } else {
                    entity = new TeachersProfessionalRankEntity();
                }

                // Update entity fields with null checks
                if (dto.getTypeOfProfessionalRank() != null) entity.setTypeOfProfessionalRank(dto.getTypeOfProfessionalRank());
                if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
                if (dto.getAnnouncementNumber() != null) entity.setAnnouncementNumber(dto.getAnnouncementNumber());
                if (dto.getDateAccepted() != null) entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(savedStaff); // Always set the parent reference

                teachersProfessionalRankRepository.save(entity);
            }
        }

        // TeacherExperience management
        if (updateDto.getTeacherExperiences() != null) {
            for (TeacherExperienceDto dto : updateDto.getTeacherExperiences()) {
                TeacherExperienceEntity entity;

                if (dto.getId() != null) {
                    Optional<TeacherExperienceEntity> existingEntity = teacherExperienceRepository.findById(dto.getId());
                    if (existingEntity.isPresent()) {
                        entity = existingEntity.get();
                    } else {
                        entity = new TeacherExperienceEntity();
                    }
                } else {
                    entity = new TeacherExperienceEntity();
                }

                // Update with null checks
                if (dto.getContinuousEmployment() != null) entity.setContinuousEmployment(dto.getContinuousEmployment());
                if (dto.getWorkPlace() != null) entity.setWorkPlace(dto.getWorkPlace());
                if (dto.getStartDate() != null) entity.setStartDate(dto.getStartDate());
                if (dto.getEndDate() != null) entity.setEndDate(dto.getEndDate());
                entity.setUser(savedStaff);

                teacherExperienceRepository.save(entity);
            }
        }

        // TeacherPraiseOrCriticism management
        if (updateDto.getTeacherPraiseOrCriticisms() != null) {
            for (TeacherPraiseOrCriticismDto dto : updateDto.getTeacherPraiseOrCriticisms()) {
                TeacherPraiseOrCriticismEntity entity;

                if (dto.getId() != null) {
                    Optional<TeacherPraiseOrCriticismEntity> existingEntity = teacherPraiseOrCriticismRepository.findById(dto.getId());
                    if (existingEntity.isPresent()) {
                        entity = existingEntity.get();
                    } else {
                        entity = new TeacherPraiseOrCriticismEntity();
                    }
                } else {
                    entity = new TeacherPraiseOrCriticismEntity();
                }

                // Update with null checks
                if (dto.getTypePraiseOrCriticism() != null) entity.setTypePraiseOrCriticism(dto.getTypePraiseOrCriticism());
                if (dto.getGiveBy() != null) entity.setGiveBy(dto.getGiveBy());
                if (dto.getDateAccepted() != null) entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(savedStaff);

                teacherPraiseOrCriticismRepository.save(entity);
            }
        }

        // TeacherEducation management
        if (updateDto.getTeacherEducations() != null) {
            for (TeacherEducationDto dto : updateDto.getTeacherEducations()) {
                TeacherEducationEntity entity;

                if (dto.getId() != null) {
                    Optional<TeacherEducationEntity> existingEntity = teacherEducationRepository.findById(dto.getId());
                    if (existingEntity.isPresent()) {
                        entity = existingEntity.get();
                    } else {
                        entity = new TeacherEducationEntity();
                    }
                } else {
                    entity = new TeacherEducationEntity();
                }

                // Update with null checks
                if (dto.getCulturalLevel() != null) entity.setCulturalLevel(dto.getCulturalLevel());
                if (dto.getSkillName() != null) entity.setSkillName(dto.getSkillName());
                if (dto.getDateAccepted() != null) entity.setDateAccepted(dto.getDateAccepted());
                if (dto.getCountry() != null) entity.setCountry(dto.getCountry());
                entity.setUser(savedStaff);

                teacherEducationRepository.save(entity);
            }
        }

        // TeacherVocational management
        if (updateDto.getTeacherVocationals() != null) {
            for (TeacherVocationalDto dto : updateDto.getTeacherVocationals()) {
                TeacherVocationalEntity entity;

                if (dto.getId() != null) {
                    Optional<TeacherVocationalEntity> existingEntity = teacherVocationalRepository.findById(dto.getId());
                    if (existingEntity.isPresent()) {
                        entity = existingEntity.get();
                    } else {
                        entity = new TeacherVocationalEntity();
                    }
                } else {
                    entity = new TeacherVocationalEntity();
                }

                // Update with null checks
                if (dto.getCulturalLevel() != null) entity.setCulturalLevel(dto.getCulturalLevel());
                if (dto.getSkillOne() != null) entity.setSkillOne(dto.getSkillOne());
                if (dto.getSkillTwo() != null) entity.setSkillTwo(dto.getSkillTwo());
                if (dto.getTrainingSystem() != null) entity.setTrainingSystem(dto.getTrainingSystem());
                if (dto.getDateAccepted() != null) entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(savedStaff);

                teacherVocationalRepository.save(entity);
            }
        }

        // TeacherShortCourse management
        if (updateDto.getTeacherShortCourses() != null) {
            for (TeacherShortCourseDto dto : updateDto.getTeacherShortCourses()) {
                TeacherShortCourseEntity entity;

                if (dto.getId() != null) {
                    Optional<TeacherShortCourseEntity> existingEntity = teacherShortCourseRepository.findById(dto.getId());
                    if (existingEntity.isPresent()) {
                        entity = existingEntity.get();
                    } else {
                        entity = new TeacherShortCourseEntity();
                    }
                } else {
                    entity = new TeacherShortCourseEntity();
                }

                // Update with null checks
                if (dto.getSkill() != null) entity.setSkill(dto.getSkill());
                if (dto.getSkillName() != null) entity.setSkillName(dto.getSkillName());
                if (dto.getStartDate() != null) entity.setStartDate(dto.getStartDate());
                if (dto.getEndDate() != null) entity.setEndDate(dto.getEndDate());
                if (dto.getDuration() != null) entity.setDuration(dto.getDuration());
                if (dto.getPreparedBy() != null) entity.setPreparedBy(dto.getPreparedBy());
                if (dto.getSupportBy() != null) entity.setSupportBy(dto.getSupportBy());
                entity.setUser(savedStaff);

                teacherShortCourseRepository.save(entity);
            }
        }

        // TeacherLanguage management
        if (updateDto.getTeacherLanguages() != null) {
            for (TeacherLanguageDto dto : updateDto.getTeacherLanguages()) {
                TeacherLanguageEntity entity;

                if (dto.getId() != null) {
                    Optional<TeacherLanguageEntity> existingEntity = teacherLanguageRepository.findById(dto.getId());
                    if (existingEntity.isPresent()) {
                        entity = existingEntity.get();
                    } else {
                        entity = new TeacherLanguageEntity();
                    }
                } else {
                    entity = new TeacherLanguageEntity();
                }

                // Update with null checks
                if (dto.getLanguage() != null) entity.setLanguage(dto.getLanguage());
                if (dto.getReading() != null) entity.setReading(dto.getReading());
                if (dto.getWriting() != null) entity.setWriting(dto.getWriting());
                if (dto.getSpeaking() != null) entity.setSpeaking(dto.getSpeaking());
                entity.setUser(savedStaff);

                teacherLanguageRepository.save(entity);
            }
        }

        // TeacherFamily management
        if (updateDto.getTeacherFamilies() != null) {
            for (TeacherFamilyDto dto : updateDto.getTeacherFamilies()) {
                TeacherFamilyEntity entity;

                if (dto.getId() != null) {
                    Optional<TeacherFamilyEntity> existingEntity = teacherFamilyRepository.findById(dto.getId());
                    if (existingEntity.isPresent()) {
                        entity = existingEntity.get();
                    } else {
                        entity = new TeacherFamilyEntity();
                    }
                } else {
                    entity = new TeacherFamilyEntity();
                }

                // Update with null checks
                if (dto.getNameChild() != null) entity.setNameChild(dto.getNameChild());
                if (dto.getGender() != null) entity.setGender(dto.getGender());
                if (dto.getDateOfBirth() != null) entity.setDateOfBirth(dto.getDateOfBirth());
                if (dto.getWorking() != null) entity.setWorking(dto.getWorking());
                entity.setUser(savedStaff);

                teacherFamilyRepository.save(entity);
            }
        }

        // Refresh the entity to get all updated relationships
        UserEntity updatedStaff = userRepository.findById(savedStaff.getId()).orElseThrow();
        log.info("Staff user with ID {} updated successfully", id);

        return staffMapper.toStaffUserDto(updatedStaff);
    }

    @Override
    @Transactional
    public StaffUserResponseDto deleteStaffUser(Long id) {
        log.info("Deleting/deactivating staff user with ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User with ID {} not found", id);
                    return new NotFoundException("User not found with ID: " + id);
                });

        // Verify user is staff type (not a student)
        if (user.isStudent()) {
            throw new BadRequestException("User with ID " + id + " is a student, not a staff user");
        }

        // Instead of hard delete, deactivate the user
        user.setStatus(Status.INACTIVE);
        UserEntity deactivatedUser = userRepository.save(user);

        log.info("Staff user with ID {} deactivated successfully", id);
        return staffMapper.toStaffUserDto(deactivatedUser);
    }
}