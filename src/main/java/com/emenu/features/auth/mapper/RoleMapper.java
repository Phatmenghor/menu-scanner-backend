package com.emenu.features.auth.mapper;

import com.emenu.features.auth.dto.request.RoleCreateRequest;
import com.emenu.features.auth.dto.response.RoleResponse;
import com.emenu.features.auth.dto.update.RoleUpdateRequest;
import com.emenu.features.auth.models.Role;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    @Mapping(target = "businessName", ignore = true)
    @Mapping(target = "roleType", expression = "java(determineRoleType(role))")
    @Mapping(target = "userCount", expression = "java(role.getUsers() != null ? role.getUsers().size() : 0)")
    RoleResponse toResponse(Role role);

    List<RoleResponse> toResponseList(List<Role> roles);

    @Mapping(target = "users", ignore = true)
    Role toEntity(RoleCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "users", ignore = true)
    void updateEntity(RoleUpdateRequest request, @MappingTarget Role role);

    default String determineRoleType(Role role) {
        if (role == null) return "UNKNOWN";
        if (role.isPlatformRole()) return "PLATFORM";
        if (role.isBusinessRole()) return "BUSINESS";
        if (role.isCustomerRole()) return "CUSTOMER";
        return "OTHER";
    }

    default PaginationResponse<RoleResponse> toPaginationResponse(Page<Role> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}
