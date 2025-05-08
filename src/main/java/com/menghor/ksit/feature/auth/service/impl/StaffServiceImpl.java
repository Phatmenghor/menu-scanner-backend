package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.dto.relationship.*;
import com.menghor.ksit.feature.auth.dto.request.StaffCreateRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StaffUpdateRequestDto;
import com.menghor.ksit.feature.auth.dto.request.StaffUserFilterRequestDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserAllResponseDto;
import com.menghor.ksit.feature.auth.dto.resposne.StaffUserResponseDto;
import com.menghor.ksit.feature.auth.mapper.StaffMapper;
import com.menghor.ksit.feature.auth.models.*;
import com.menghor.ksit.feature.auth.repository.RoleRepository;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.auth.repository.UserSpecification;
import com.menghor.ksit.feature.auth.service.StaffService;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.repository.DepartmentRepository;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final StaffMapper staffMapper;

    @Override
    @Transactional
    public StaffUserResponseDto registerStaff(StaffCreateRequestDto requestDto) {
        log.info("Registering new staff user with email: {}", requestDto.getEmail());
        
        // Check if username already exists
        if (userRepository.existsByUsername(requestDto.getEmail())) {
            log.warn("Attempt to register with duplicate email: {}", requestDto.getEmail());
            throw new DuplicateNameException("Email is already in use");
        }

        UserEntity staff = new UserEntity();

        // Set common fields
        staff.setUsername(requestDto.getEmail());
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

        // Set staff-specific fields
        staff.setStaffId(requestDto.getStaffId());
        staff.setNationalId(requestDto.getNationalId());
        staff.setIdentifyNumber(requestDto.getIdentifyNumber());
        staff.setStartWorkDate(requestDto.getStartWorkDate());
        staff.setCurrentPositionDate(requestDto.getCurrentPositionDate());
        staff.setEmployeeWork(requestDto.getEmployeeWork());
        staff.setDisability(requestDto.getDisability());
        staff.setPayroll_account_number(requestDto.getPayroll_account_number());
        staff.setCpp_membership_number(requestDto.getCpp_membership_number());
        staff.setProvince(requestDto.getProvince());
        staff.setDistrict(requestDto.getDistrict());
        staff.setCommune(requestDto.getCommune());
        staff.setVillage(requestDto.getVillage());
        staff.setOfficeName(requestDto.getOfficeName());
        staff.setCurrentPosition(requestDto.getCurrentPosition());
        staff.setDecreeFinal(requestDto.getDecreeFinal());
        staff.setRankAndClass(requestDto.getRankAndClass());
        
        // Set work history and family information
        staff.setWorkHistory(requestDto.getWorkHistory());
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
        
        // Handle related entity lists
        if (requestDto.getTeachersProfessionalRanks() != null && !requestDto.getTeachersProfessionalRanks().isEmpty()) {
            List<TeachersProfessionalRankEntity> rankEntities = new ArrayList<>();
            for (TeachersProfessionalRankDto dto : requestDto.getTeachersProfessionalRanks()) {
                TeachersProfessionalRankEntity entity = new TeachersProfessionalRankEntity();
                entity.setTypeOfProfessionalRank(dto.getTypeOfProfessionalRank());
                entity.setDescription(dto.getDescription());
                entity.setAnnouncementNumber(dto.getAnnouncementNumber());
                entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(staff);
                rankEntities.add(entity);
            }
            staff.setTeachersProfessionalRank(rankEntities);
        }
        
        // Handle teacher experience entries
        if (requestDto.getTeacherExperiences() != null && !requestDto.getTeacherExperiences().isEmpty()) {
            List<TeacherExperienceEntity> experienceEntities = new ArrayList<>();
            for (TeacherExperienceDto dto : requestDto.getTeacherExperiences()) {
                TeacherExperienceEntity entity = new TeacherExperienceEntity();
                entity.setContinuousEmployment(dto.getContinuousEmployment());
                entity.setWorkPlace(dto.getWorkPlace());
                entity.setStartDate(dto.getStartDate());
                entity.setEndDate(dto.getEndDate());
                entity.setUser(staff);
                experienceEntities.add(entity);
            }
            staff.setTeacherExperience(experienceEntities);
        }
        
        // Handle praise/criticism entries
        if (requestDto.getTeacherPraiseOrCriticisms() != null && !requestDto.getTeacherPraiseOrCriticisms().isEmpty()) {
            List<TeacherPraiseOrCriticismEntity> praiseEntities = new ArrayList<>();
            for (TeacherPraiseOrCriticismDto dto : requestDto.getTeacherPraiseOrCriticisms()) {
                TeacherPraiseOrCriticismEntity entity = new TeacherPraiseOrCriticismEntity();
                entity.setTypePraiseOrCriticism(dto.getTypePraiseOrCriticism());
                entity.setGiveBy(dto.getGiveBy());
                entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(staff);
                praiseEntities.add(entity);
            }
            staff.setTeacherPraiseOrCriticism(praiseEntities);
        }
        
        // Handle education entries
        if (requestDto.getTeacherEducations() != null && !requestDto.getTeacherEducations().isEmpty()) {
            List<TeacherEducationEntity> educationEntities = new ArrayList<>();
            for (TeacherEducationDto dto : requestDto.getTeacherEducations()) {
                TeacherEducationEntity entity = new TeacherEducationEntity();
                entity.setCulturalLevel(dto.getCulturalLevel());
                entity.setSkillName(dto.getSkillName());
                entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(staff);
                educationEntities.add(entity);
            }
            staff.setTeacherEducation(educationEntities);
        }
        
        // Handle vocational entries
        if (requestDto.getTeacherVocationals() != null && !requestDto.getTeacherVocationals().isEmpty()) {
            List<TeacherVocationalEntity> vocationalEntities = new ArrayList<>();
            for (TeacherVocationalDto dto : requestDto.getTeacherVocationals()) {
                TeacherVocationalEntity entity = new TeacherVocationalEntity();
                entity.setCulturalLevel(dto.getCulturalLevel());
                entity.setSkillOne(dto.getSkillOne());
                entity.setSkillTwo(dto.getSkillTwo());
                entity.setTrainingSystem(dto.getTrainingSystem());
                entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(staff);
                vocationalEntities.add(entity);
            }
            staff.setTeacherVocational(vocationalEntities);
        }
        
        // Handle short course entries
        if (requestDto.getTeacherShortCourses() != null && !requestDto.getTeacherShortCourses().isEmpty()) {
            List<TeacherShortCourseEntity> courseEntities = new ArrayList<>();
            for (TeacherShortCourseDto dto : requestDto.getTeacherShortCourses()) {
                TeacherShortCourseEntity entity = new TeacherShortCourseEntity();
                entity.setSkill(dto.getSkill());
                entity.setSkillName(dto.getSkillName());
                entity.setStartDate(dto.getStartDate());
                entity.setEndDate(dto.getEndDate());
                entity.setDuration(dto.getDuration());
                entity.setPreparedBy(dto.getPreparedBy());
                entity.setSupportBy(dto.getSupportBy());
                entity.setUser(staff);
                courseEntities.add(entity);
            }
            staff.setTeacherShortCourse(courseEntities);
        }
        
        // Handle language entries
        if (requestDto.getTeacherLanguages() != null && !requestDto.getTeacherLanguages().isEmpty()) {
            List<TeacherLanguageEntity> languageEntities = new ArrayList<>();
            for (TeacherLanguageDto dto : requestDto.getTeacherLanguages()) {
                TeacherLanguageEntity entity = new TeacherLanguageEntity();
                entity.setLanguage(dto.getLanguage());
                entity.setReading(dto.getReading());
                entity.setWriting(dto.getWriting());
                entity.setSpeaking(dto.getSpeaking());
                entity.setUser(staff);
                languageEntities.add(entity);
            }
            staff.setTeacherLanguage(languageEntities);
        }
        
        // Handle family entries
        if (requestDto.getTeacherFamilies() != null && !requestDto.getTeacherFamilies().isEmpty()) {
            List<TeacherFamilyEntity> familyEntities = new ArrayList<>();
            for (TeacherFamilyDto dto : requestDto.getTeacherFamilies()) {
                TeacherFamilyEntity entity = new TeacherFamilyEntity();
                entity.setNameChild(dto.getNameChild());
                entity.setGender(dto.getGender());
                entity.setDateOfBirth(dto.getDateOfBirth());
                entity.setWorking(dto.getWorking());
                entity.setUser(staff);
                familyEntities.add(entity);
            }
            staff.setTeacherFamily(familyEntities);
        }

        UserEntity savedStaff = userRepository.save(staff);
        log.info("Staff user registered successfully with ID: {}", savedStaff.getId());

        return staffMapper.toStaffUserDto(savedStaff);
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
        List<StaffUserResponseDto> userDtos = staffMapper.toStaffUserDtoList(userPage.getContent());

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

        // Check if email is changing and if it conflicts
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(staff.getUsername()) && 
                userRepository.existsByUsername(updateDto.getEmail())) {
            throw new DuplicateNameException("Email is already in use");
        }

        // Update basic fields if provided
        if (updateDto.getEmail() != null) {
            staff.setUsername(updateDto.getEmail());
            staff.setEmail(updateDto.getEmail());
        }

        // Update personal information
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

        // Update staff-specific fields
        if (updateDto.getStaffId() != null) staff.setStaffId(updateDto.getStaffId());
        if (updateDto.getNationalId() != null) staff.setNationalId(updateDto.getNationalId());
        if (updateDto.getIdentifyNumber() != null) staff.setIdentifyNumber(updateDto.getIdentifyNumber());
        if (updateDto.getStartWorkDate() != null) staff.setStartWorkDate(updateDto.getStartWorkDate());
        if (updateDto.getCurrentPositionDate() != null) staff.setCurrentPositionDate(updateDto.getCurrentPositionDate());
        if (updateDto.getEmployeeWork() != null) staff.setEmployeeWork(updateDto.getEmployeeWork());
        if (updateDto.getDisability() != null) staff.setDisability(updateDto.getDisability());
        if (updateDto.getPayroll_account_number() != null) staff.setPayroll_account_number(updateDto.getPayroll_account_number());
        if (updateDto.getCpp_membership_number() != null) staff.setCpp_membership_number(updateDto.getCpp_membership_number());
        if (updateDto.getProvince() != null) staff.setProvince(updateDto.getProvince());
        if (updateDto.getDistrict() != null) staff.setDistrict(updateDto.getDistrict());
        if (updateDto.getCommune() != null) staff.setCommune(updateDto.getCommune());
        if (updateDto.getVillage() != null) staff.setVillage(updateDto.getVillage());
        if (updateDto.getOfficeName() != null) staff.setOfficeName(updateDto.getOfficeName());
        if (updateDto.getCurrentPosition() != null) staff.setCurrentPosition(updateDto.getCurrentPosition());
        if (updateDto.getDecreeFinal() != null) staff.setDecreeFinal(updateDto.getDecreeFinal());
        if (updateDto.getRankAndClass() != null) staff.setRankAndClass(updateDto.getRankAndClass());

        // Update work history and family information
        if (updateDto.getWorkHistory() != null) staff.setWorkHistory(updateDto.getWorkHistory());
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
        
        // TeachersProfessionalRank management
        if (updateDto.getTeachersProfessionalRanks() != null) {
            // Clear existing records and add new ones
            staff.getTeachersProfessionalRank().clear();
            
            for (TeachersProfessionalRankDto dto : updateDto.getTeachersProfessionalRanks()) {
                TeachersProfessionalRankEntity entity;
                
                // If ID exists, try to find existing entity
                if (dto.getId() != null) {
                    // Look for the entity in the existing set
                    entity = staff.getTeachersProfessionalRank().stream()
                            .filter(e -> e.getId().equals(dto.getId()))
                            .findFirst()
                            .orElse(new TeachersProfessionalRankEntity());
                } else {
                    entity = new TeachersProfessionalRankEntity();
                }
                
                // Update entity fields
                entity.setTypeOfProfessionalRank(dto.getTypeOfProfessionalRank());
                entity.setDescription(dto.getDescription());
                entity.setAnnouncementNumber(dto.getAnnouncementNumber());
                entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(staff);
                
                staff.getTeachersProfessionalRank().add(entity);
            }
        }
        
        // TeacherExperience management
        if (updateDto.getTeacherExperiences() != null) {
            // Clear existing records and add new ones
            staff.getTeacherExperience().clear();
            
            for (TeacherExperienceDto dto : updateDto.getTeacherExperiences()) {
                TeacherExperienceEntity entity;
                
                if (dto.getId() != null) {
                    entity = staff.getTeacherExperience().stream()
                            .filter(e -> e.getId().equals(dto.getId()))
                            .findFirst()
                            .orElse(new TeacherExperienceEntity());
                } else {
                    entity = new TeacherExperienceEntity();
                }
                
                entity.setContinuousEmployment(dto.getContinuousEmployment());
                entity.setWorkPlace(dto.getWorkPlace());
                entity.setStartDate(dto.getStartDate());
                entity.setEndDate(dto.getEndDate());
                entity.setUser(staff);
                
                staff.getTeacherExperience().add(entity);
            }
        }
        
        // TeacherPraiseOrCriticism management
        if (updateDto.getTeacherPraiseOrCriticisms() != null) {
            staff.getTeacherPraiseOrCriticism().clear();
            
            for (TeacherPraiseOrCriticismDto dto : updateDto.getTeacherPraiseOrCriticisms()) {
                TeacherPraiseOrCriticismEntity entity;
                
                if (dto.getId() != null) {
                    entity = staff.getTeacherPraiseOrCriticism().stream()
                            .filter(e -> e.getId().equals(dto.getId()))
                            .findFirst()
                            .orElse(new TeacherPraiseOrCriticismEntity());
                } else {
                    entity = new TeacherPraiseOrCriticismEntity();
                }
                
                entity.setTypePraiseOrCriticism(dto.getTypePraiseOrCriticism());
                entity.setGiveBy(dto.getGiveBy());
                entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(staff);
                
                staff.getTeacherPraiseOrCriticism().add(entity);
            }
        }
        
        // TeacherEducation management
        if (updateDto.getTeacherEducations() != null) {
            staff.getTeacherEducation().clear();
            
            for (TeacherEducationDto dto : updateDto.getTeacherEducations()) {
                TeacherEducationEntity entity;
                
                if (dto.getId() != null) {
                    entity = staff.getTeacherEducation().stream()
                            .filter(e -> e.getId().equals(dto.getId()))
                            .findFirst()
                            .orElse(new TeacherEducationEntity());
                } else {
                    entity = new TeacherEducationEntity();
                }
                
                entity.setCulturalLevel(dto.getCulturalLevel());
                entity.setSkillName(dto.getSkillName());
                entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(staff);
                
                staff.getTeacherEducation().add(entity);
            }
        }
        
        // TeacherVocational management
        if (updateDto.getTeacherVocationals() != null) {
            staff.getTeacherVocational().clear();
            
            for (TeacherVocationalDto dto : updateDto.getTeacherVocationals()) {
                TeacherVocationalEntity entity;
                
                if (dto.getId() != null) {
                    entity = staff.getTeacherVocational().stream()
                            .filter(e -> e.getId().equals(dto.getId()))
                            .findFirst()
                            .orElse(new TeacherVocationalEntity());
                } else {
                    entity = new TeacherVocationalEntity();
                }
                
                entity.setCulturalLevel(dto.getCulturalLevel());
                entity.setSkillOne(dto.getSkillOne());
                entity.setSkillTwo(dto.getSkillTwo());
                entity.setTrainingSystem(dto.getTrainingSystem());
                entity.setDateAccepted(dto.getDateAccepted());
                entity.setUser(staff);
                
                staff.getTeacherVocational().add(entity);
            }
        }
        
        // TeacherShortCourse management
        if (updateDto.getTeacherShortCourses() != null) {
            staff.getTeacherShortCourse().clear();
            
            for (TeacherShortCourseDto dto : updateDto.getTeacherShortCourses()) {
                TeacherShortCourseEntity entity;
                
                if (dto.getId() != null) {
                    entity = staff.getTeacherShortCourse().stream()
                            .filter(e -> e.getId().equals(dto.getId()))
                            .findFirst()
                            .orElse(new TeacherShortCourseEntity());
                } else {
                    entity = new TeacherShortCourseEntity();
                }
                
                entity.setSkill(dto.getSkill());
                entity.setSkillName(dto.getSkillName());
                entity.setStartDate(dto.getStartDate());
                entity.setEndDate(dto.getEndDate());
                entity.setDuration(dto.getDuration());
                entity.setPreparedBy(dto.getPreparedBy());
                entity.setSupportBy(dto.getSupportBy());
                entity.setUser(staff);
                
                staff.getTeacherShortCourse().add(entity);
            }
        }
        
        // TeacherLanguage management
        if (updateDto.getTeacherLanguages() != null) {
            staff.getTeacherLanguage().clear();
            
            for (TeacherLanguageDto dto : updateDto.getTeacherLanguages()) {
                TeacherLanguageEntity entity;
                
                if (dto.getId() != null) {
                    entity = staff.getTeacherLanguage().stream()
                            .filter(e -> e.getId().equals(dto.getId()))
                            .findFirst()
                            .orElse(new TeacherLanguageEntity());
                } else {
                    entity = new TeacherLanguageEntity();
                }
                
                entity.setLanguage(dto.getLanguage());
                entity.setReading(dto.getReading());
                entity.setWriting(dto.getWriting());
                entity.setSpeaking(dto.getSpeaking());
                entity.setUser(staff);
                
                staff.getTeacherLanguage().add(entity);
            }
        }
        
        // TeacherFamily management
        if (updateDto.getTeacherFamilies() != null) {
            staff.getTeacherFamily().clear();
            
            for (TeacherFamilyDto dto : updateDto.getTeacherFamilies()) {
                TeacherFamilyEntity entity;
                
                if (dto.getId() != null) {
                    entity = staff.getTeacherFamily().stream()
                            .filter(e -> e.getId().equals(dto.getId()))
                            .findFirst()
                            .orElse(new TeacherFamilyEntity());
                } else {
                    entity = new TeacherFamilyEntity();
                }
                
                entity.setNameChild(dto.getNameChild());
                entity.setGender(dto.getGender());
                entity.setDateOfBirth(dto.getDateOfBirth());
                entity.setWorking(dto.getWorking());
                entity.setUser(staff);
                
                staff.getTeacherFamily().add(entity);
            }
        }

        UserEntity updatedStaff = userRepository.save(staff);
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