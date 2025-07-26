package com.emenu.features.auth.mapper;

import com.emenu.enums.user.RoleEnum;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {

    @Autowired
    protected PaginationMapper paginationMapper;

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
    @Mapping(target = "password", ignore = true)
    public abstract User toEntity(UserCreateRequest request);

    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToRoleEnums")
    public abstract UserResponse toResponse(User user);

    public abstract List<UserResponse> toResponseList(List<User> users);

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
    public abstract void updateEntity(UserUpdateRequest request, @MappingTarget User user);

    /**
     * Restricted update for current user profile - only allows safe fields
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userType", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "accountStatus", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    public abstract void updateCurrentUserProfile(UserUpdateRequest request, @MappingTarget User user);

    @Named("rolesToRoleEnums")
    protected List<RoleEnum> rolesToRoleEnums(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    @AfterMapping
    protected void setFullName(@MappingTarget UserResponse response, User user) {
        response.setFullName(user.getFullName());
    }

    // ✅ UNIVERSAL PAGINATION MAPPER USAGE
    public PaginationResponse<UserResponse> toPaginationResponse(Page<User> userPage) {
        return paginationMapper.toPaginationResponse(userPage, this::toResponseList);
    }
}