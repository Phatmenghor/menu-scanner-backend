package com.emenu.features.usermanagement.mapper;

import com.emenu.features.usermanagement.domain.User;
import com.emenu.features.usermanagement.dto.request.CreateUserRequest;
import com.emenu.features.usermanagement.dto.response.UserResponse;
import com.emenu.features.usermanagement.dto.response.UserSummaryResponse;
import com.emenu.features.usermanagement.dto.update.UpdateUserRequest;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "accountStatus", constant = "PENDING_VERIFICATION")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "loyaltyPoints", constant = "0")
    @Mapping(target = "totalOrders", constant = "0")
    @Mapping(target = "totalSpent", constant = "0.0")
    @Mapping(target = "loginAttempts", constant = "0")
    @Mapping(target = "twoFactorEnabled", constant = "false")
    User toEntity(CreateUserRequest request);

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user))")
    UserResponse toResponse(User user);

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserSummaryResponse toSummaryResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    List<UserSummaryResponse> toSummaryResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);

    default List<String> mapRolesToStrings(User user) {
        if (user.getRoles() == null) {
            return List.of();
        }
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
    }
}