package com.emenu.features.user_management.repository;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.RoleEnum;
import com.emenu.enums.UserType;
import com.emenu.features.user_management.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailAndIsDeletedFalse(String email);
    
    Optional<User> findByIdAndIsDeletedFalse(UUID id);
    
    boolean existsByEmailAndIsDeletedFalse(String email);
    
    boolean existsByPhoneNumberAndIsDeletedFalse(String phoneNumber);
    
    Page<User> findByIsDeletedFalse(Pageable pageable);
    
    Page<User> findByUserTypeAndIsDeletedFalse(UserType userType, Pageable pageable);
    
    Page<User> findByAccountStatusAndIsDeletedFalse(AccountStatus accountStatus, Pageable pageable);
    
    List<User> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    Page<User> findByBusinessIdAndIsDeletedFalse(UUID businessId, Pageable pageable);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role AND u.isDeleted = false")
    Page<User> findByRoleAndIsDeletedFalse(@Param("role") RoleEnum role, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findBySearchAndIsDeletedFalse(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.isDeleted = false")
    long countByUserType(@Param("userType") UserType userType);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.businessId = :businessId AND u.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);
}