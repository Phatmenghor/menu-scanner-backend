
package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.request.PlatformUserCreateRequest;
import com.emenu.features.auth.dto.response.PlatformUserResponse;
import com.emenu.features.auth.dto.update.PlatformUserUpdateRequest;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlatformMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userType", constant = "PLATFORM_USER")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    User toEntity(PlatformUserCreateRequest request);

    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToRoleEnums")
    PlatformUserResponse toResponse(User user);

    List<PlatformUserResponse> toResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userType", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(PlatformUserUpdateRequest request, @MappingTarget User user);

    @Named("rolesToRoleEnums")
    default List<com.emenu.enums.RoleEnum> rolesToRoleEnums(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toList());
    }

    @AfterMapping
    default void setFullName(@MappingTarget PlatformUserResponse response, User user) {
        response.setFullName(user.getFullName());
    }
}