package com.emenu.features.user_management.mapper;

import com.emenu.features.user_management.domain.User;
import com.emenu.features.user_management.dto.response.PlatformUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PlatformUserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    @Mapping(target = "roles", expression = "java(mapRoles(user))")
    PlatformUserResponse toResponse(User user);

    default List<String> mapRoles(User user) {
        if (user.getRoles() == null) return List.of();
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
    }
}
