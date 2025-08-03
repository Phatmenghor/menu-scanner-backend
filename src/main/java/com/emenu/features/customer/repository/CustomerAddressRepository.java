package com.emenu.features.customer.repository;

import com.emenu.features.customer.models.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {
    
    List<CustomerAddress> findByUserIdAndIsDeletedFalseOrderByIsDefaultDescCreatedAtDesc(UUID userId);
    
    Optional<CustomerAddress> findByUserIdAndIsDefaultTrueAndIsDeletedFalse(UUID userId);
    
    @Modifying
    @Query("UPDATE CustomerAddress ca SET ca.isDefault = false WHERE ca.userId = :userId AND ca.isDeleted = false")
    void clearDefaultForUser(@Param("userId") UUID userId);
    
    Optional<CustomerAddress> findByIdAndIsDeletedFalse(UUID id);
}
