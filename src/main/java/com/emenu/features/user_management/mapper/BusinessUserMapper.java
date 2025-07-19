package com.emenu.features.user_management.mapper;

import com.emenu.features.user_management.domain.User;
import com.emenu.features.user_management.dto.response.BusinessUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BusinessUserMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    @Mapping(target = "role", expression = "java(user.getRoles().isEmpty() ? null : user.getRoles().get(0).getName().name())")
    BusinessUserResponse toResponse(User user);
}