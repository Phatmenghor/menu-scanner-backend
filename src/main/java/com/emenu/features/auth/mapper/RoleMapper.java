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
    RoleResponse toResponse(Role role);

    List<RoleResponse> toResponseList(List<Role> roles);

    Role toEntity(RoleCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(RoleUpdateRequest request, @MappingTarget Role role);

    default PaginationResponse<RoleResponse> toPaginationResponse(Page<Role> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}
