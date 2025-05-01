package com.menghor.ksit.feature.auth.mapper;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.dto.request.UserRegisterDto;
import com.menghor.ksit.feature.auth.dto.request.StudentRegisterDto;
import com.menghor.ksit.feature.auth.models.UserEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public abstract class UserRegistrationMapper {

    /**
     * Map student register DTO to UserEntity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "status", source = "status", defaultValue = "ACTIVE")
    public abstract UserEntity toEntityFromStudentRegister(StudentRegisterDto registerDto);

    /**
     * Map advanced register DTO to UserEntity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "status", source = "status", defaultValue = "ACTIVE")
    public abstract UserEntity toEntityFromAdvancedRegister(UserRegisterDto registerDto);

    /**
     * Custom method to set default role if not provided
     */
    protected RoleEnum mapDefaultRole(RoleEnum role) {
        return role != null ? role : RoleEnum.STUDENT;
    }

    /**
     * Custom method to ensure status is never null
     */
    protected Status mapStatus(Status status) {
        return status != null ? status : Status.ACTIVE;
    }

    /**
     * Enhance mapping with additional logic
     */
    @AfterMapping
    protected void enrichUserEntity(
            @MappingTarget UserEntity userEntity,
            Object registerDto
    ) {
        // Additional enrichment logic can be added here
        if (userEntity.getStatus() == null) {
            userEntity.setStatus(Status.ACTIVE);
        }

        // You can add more complex mapping logic here if needed
    }
}