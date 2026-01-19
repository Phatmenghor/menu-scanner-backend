package com.emenu.features.auth.mapper;

import com.emenu.enums.user.RoleEnum;
import com.emenu.features.auth.dto.request.RegisterRequest;
import com.emenu.features.auth.dto.request.UserCreateRequest;
import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.dto.response.UserBasicInfo;
import com.emenu.features.auth.dto.response.UserResponse;
import com.emenu.features.auth.dto.update.UserUpdateRequest;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "businessName", source = "business.name")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToEnums")
    UserResponse toResponse(User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "profileImageUrl", source = "profileImageUrl")
    UserBasicInfo toUserBasicInfo(User user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "roles", source = "user.roles", qualifiedByName = "rolesToStrings")
    @Mapping(target = "businessName", source = "user.business.name")
    @Mapping(target = "accessToken", source = "token")
    @Mapping(target = "tokenType", constant = "Bearer")
    LoginResponse toLoginResponse(User user, String token);

    List<UserResponse> toResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "notes", ignore = true)
    User toEntity(RegisterRequest request);

    @Named("rolesToEnums")
    default List<RoleEnum> rolesToEnums(List<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream().map(Role::getName).collect(Collectors.toList());
    }

    @Named("rolesToStrings")
    default List<String> rolesToStrings(List<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream().map(role -> role.getName().name()).collect(Collectors.toList());
    }

    default PaginationResponse<UserResponse> toPaginationResponse(Page<User> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}