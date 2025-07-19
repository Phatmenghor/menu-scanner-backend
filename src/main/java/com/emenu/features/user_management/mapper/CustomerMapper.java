package com.emenu.features.user_management.mapper;

import com.emenu.features.user_management.domain.User;
import com.emenu.features.user_management.dto.response.CustomerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    @Mapping(target = "customerTier", expression = "java(user.getCustomerTier().name())")
    CustomerResponse toResponse(User user);
}
