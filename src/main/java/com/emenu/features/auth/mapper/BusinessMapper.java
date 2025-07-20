package com.emenu.features.auth.mapper;

import com.emenu.enums.RoleEnum;
import com.emenu.features.auth.dto.request.BusinessCreateRequest;
import com.emenu.features.auth.dto.response.BusinessResponse;
import com.emenu.features.auth.dto.response.BusinessStaffResponse;
import com.emenu.features.auth.dto.update.BusinessUpdateRequest;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Business toEntity(BusinessCreateRequest request);

    BusinessResponse toResponse(Business business);
    List<BusinessResponse> toResponseList(List<Business> businesses);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntity(BusinessUpdateRequest request, @MappingTarget Business business);

    @Mapping(source = "business.name", target = "businessName")
    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToRoleEnums")
    BusinessStaffResponse toStaffResponse(User user);

    List<BusinessStaffResponse> toStaffResponseList(List<User> users);

    @Named("rolesToRoleEnums")
    default List<RoleEnum> rolesToRoleEnums(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toList());
    }

    @AfterMapping
    default void setStaffFullName(@MappingTarget BusinessStaffResponse response, User user) {
        response.setFullName(user.getFullName());
    }
}