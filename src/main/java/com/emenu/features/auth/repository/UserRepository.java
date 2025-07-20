package com.emenu.features.auth.repository;

import com.emenu.enums.AccountStatus;
import com.emenu.enums.UserType;
import com.emenu.features.auth.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailAndIsDeletedFalse(String email);
    
    Optional<User> findByIdAndIsDeletedFalse(UUID id);
    
    boolean existsByEmailAndIsDeletedFalse(String email);
    
    boolean existsByPhoneNumberAndIsDeletedFalse(String phoneNumber);
    
    List<User> findByIsDeletedFalse();
    
    Page<User> findByIsDeletedFalse(Pageable pageable);
    
    Page<User> findByUserTypeAndIsDeletedFalse(UserType userType, Pageable pageable);
    
    Page<User> findByAccountStatusAndIsDeletedFalse(AccountStatus accountStatus, Pageable pageable);
    
    List<User> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    Page<User> findByBusinessIdAndIsDeletedFalse(UUID businessId, Pageable pageable);
    
    List<User> findByUserTypeInAndIsDeletedFalse(List<UserType> userTypes);
    
    List<User> findByBusinessIdInAndIsDeletedFalse(List<UUID> businessIds);
    
    List<User> findByIdInAndIsDeletedFalse(List<UUID> ids);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.isDeleted = false")
    long countByUserTypeAndIsDeletedFalse(@Param("userType") UserType userType);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.businessId = :businessId AND u.isDeleted = false")
    long countByBusinessIdAndIsDeletedFalse(@Param("businessId") UUID businessId);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isDeleted = false")
    long countByIsDeletedFalse();
    
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findBySearchAndIsDeletedFalse(@Param("search") String search, Pageable pageable);
}