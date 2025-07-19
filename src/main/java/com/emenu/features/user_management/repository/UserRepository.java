package com.emenu.features.user_management.repository;

import com.emenu.enums.Status;
import com.emenu.enums.UserType;
import com.emenu.features.user_management.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    
    // Basic queries
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    Optional<User> findByIdAndIsDeletedFalse(UUID id);
    boolean existsByEmailAndIsDeletedFalse(String email);
    boolean existsByIdAndIsDeletedFalse(UUID id);
    
    // Authentication tokens
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
    
    // User type queries
    long countByUserTypeAndIsDeletedFalse(UserType userType);
    long countByStatusAndIsDeletedFalse(Status status);
}
