package com.emenu.features.auth.mapper;

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

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userType", constant = "CUSTOMER")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "accountStatus", constant = "ACTIVE")
    @Mapping(target = "notes", ignore = true)
    User toEntity(UserCreateRequest request);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToRoleEnums")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userType", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "accountStatus", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);

    /**
     * Restricted update for current customer profile - only allows safe fields
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userType", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "accountStatus", ignore = true)
    @Mapping(target = "notes", ignore = true)
    void updateCurrentUserProfile(UserUpdateRequest request, @MappingTarget User user);

    @Named("rolesToRoleEnums")
    default List<String> rolesToRoleEnums(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    default PaginationResponse<UserResponse> toPaginationResponse(Page<User> customerPage, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(customerPage, this::toResponseList);
    }
}