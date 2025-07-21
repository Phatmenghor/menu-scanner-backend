package com.emenu.features.notification.repository;

import com.emenu.enums.notification.TemplateName;
import com.emenu.features.notification.models.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, UUID> {
    
    Optional<MessageTemplate> findByIdAndIsDeletedFalse(UUID id);
    
    Optional<MessageTemplate> findByTemplateNameAndIsDeletedFalse(TemplateName templateName);
    
    Optional<MessageTemplate> findByTemplateNameAndLanguageAndIsDeletedFalse(TemplateName templateName, String language);
    
    List<MessageTemplate> findByIsActiveAndIsDeletedFalse(Boolean isActive);
    
    List<MessageTemplate> findByLanguageAndIsDeletedFalse(String language);
    
    @Query("SELECT mt FROM MessageTemplate mt WHERE mt.isDeleted = false ORDER BY mt.templateName")
    List<MessageTemplate> findAllActiveTemplates();
    
    boolean existsByTemplateNameAndIsDeletedFalse(TemplateName templateName);
    
    boolean existsByTemplateNameAndLanguageAndIsDeletedFalse(TemplateName templateName, String language);
    
    @Query("SELECT COUNT(mt) FROM MessageTemplate mt WHERE mt.isActive = true AND mt.isDeleted = false")
    long countActiveTemplates();
}