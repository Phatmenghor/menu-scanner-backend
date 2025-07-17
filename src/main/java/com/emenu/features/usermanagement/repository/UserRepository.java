package com.emenu.features.usermanagement.repository;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.RoleEnum;
import com.emenu.features.usermanagement.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailAndIsDeletedFalse(String email);
    
    Optional<User> findByIdAndIsDeletedFalse(UUID id);
    
    boolean existsByEmailAndIsDeletedFalse(String email);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    Optional<User> findByPasswordResetToken(String token);
    
    Page<User> findByIsDeletedFalse(Pageable pageable);
    
    List<User> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role AND u.isDeleted = false")
    List<User> findByRoleAndIsDeletedFalse(@Param("role") RoleEnum role);
    
    @Query("SELECT u FROM User u WHERE u.accountStatus = :status AND u.isDeleted = false")
    List<User> findByAccountStatusAndIsDeletedFalse(@Param("status") AccountStatus status);
    
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil < :now AND u.accountStatus = 'LOCKED' AND u.isDeleted = false")
    List<User> findUsersToUnlock(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.accountStatus = 'ACTIVE' AND u.isDeleted = false")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :role AND u.isDeleted = false")
    long countByRole(@Param("role") RoleEnum role);
}