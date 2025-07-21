package com.emenu.features.notification.mapper;

import com.emenu.features.auth.dto.response.LoginResponse;
import com.emenu.features.auth.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AuthMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "userType", source = "user.userType")
    @Mapping(target = "roles", source = "user", qualifiedByName = "mapUserRoles")
    @Mapping(target = "businessId", source = "user.businessId")
    @Mapping(target = "businessName", source = "user.business.name")
    @Mapping(target = "welcomeMessage", expression = "java(\"Welcome back, \" + user.getFirstName() + \"!\")")
    @Mapping(target = "accessToken", source = "token")
    @Mapping(target = "tokenType", constant = "Bearer")
    public abstract LoginResponse toLoginResponse(User user, String token);

    @Named("mapUserRoles")
    protected List<String> mapUserRoles(User user) {
        if (user.getRoles() == null) {
            return List.of();
        }
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
    }
}