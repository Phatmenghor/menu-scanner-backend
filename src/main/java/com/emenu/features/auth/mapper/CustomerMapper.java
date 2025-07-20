
package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.request.CustomerCreateRequest;
import com.emenu.features.auth.dto.response.CustomerResponse;
import com.emenu.features.auth.dto.update.CustomerUpdateRequest;
import com.emenu.features.auth.models.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userType", constant = "CUSTOMER")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    User toEntity(CustomerCreateRequest request);

    CustomerResponse toResponse(User user);
    List<CustomerResponse> toResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "userType", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(CustomerUpdateRequest request, @MappingTarget User user);

    @AfterMapping
    default void setFullName(@MappingTarget CustomerResponse response, User user) {
        response.setFullName(user.getFullName());
    }
}