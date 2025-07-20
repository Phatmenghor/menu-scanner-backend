package com.emenu.features.auth.mapper;

import com.emenu.enums.RoleEnum;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.response.UserSummaryResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    User toEntity(UserCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToRoleEnums")
    UserResponse toResponse(User user);

    @Mapping(source = "business.name", target = "businessName")
    UserSummaryResponse toSummaryResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
    List<UserSummaryResponse> toSummaryResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userType", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);

    @Named("rolesToRoleEnums")
    default List<RoleEnum> rolesToRoleEnums(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    @AfterMapping
    default void setFullName(@MappingTarget UserResponse response, User user) {
        response.setFullName(user.getFullName());
    }

    @AfterMapping
    default void setFullNameSummary(@MappingTarget UserSummaryResponse response, User user) {
        response.setFullName(user.getFullName());
    }
}