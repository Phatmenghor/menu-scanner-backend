package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.request.LeaveCreateRequest;
import com.emenu.features.hr.dto.response.LeaveResponse;
import com.emenu.features.hr.dto.update.LeaveUpdateRequest;
import com.emenu.features.hr.models.Leave;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class})
public interface LeaveMapper {

    @Mapping(target = "userInfo.id", source = "user.id")
    @Mapping(target = "userInfo.userIdentifier", source = "user.userIdentifier")
    @Mapping(target = "userInfo.fullName", expression = "java(leave.getUser() != null ? leave.getUser().getFullName() : null)")
    @Mapping(target = "userInfo.email", source = "user.email")
    @Mapping(target = "userInfo.profileImageUrl", source = "user.profileImageUrl")
    @Mapping(target = "actionUserInfo.id", source = "actionUser.id")
    @Mapping(target = "actionUserInfo.userIdentifier", source = "actionUser.userIdentifier")
    @Mapping(target = "actionUserInfo.fullName", expression = "java(leave.getActionUser() != null ? leave.getActionUser().getFullName() : null)")
    @Mapping(target = "actionUserInfo.email", source = "actionUser.email")
    @Mapping(target = "actionUserInfo.profileImageUrl", source = "actionUser.profileImageUrl")
    LeaveResponse toResponse(Leave leave);

    List<LeaveResponse> toResponseList(List<Leave> leaves);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "auditorAware", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "totalDays", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "actionBy", ignore = true)
    @Mapping(target = "actionUser", ignore = true)
    @Mapping(target = "actionAt", ignore = true)
    @Mapping(target = "actionNote", ignore = true)
    Leave toEntity(LeaveCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "auditorAware", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "businessId", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "leaveTypeEnum", ignore = true)
    @Mapping(target = "totalDays", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "actionBy", ignore = true)
    @Mapping(target = "actionUser", ignore = true)
    @Mapping(target = "actionAt", ignore = true)
    @Mapping(target = "actionNote", ignore = true)
    void updateEntity(LeaveUpdateRequest request, @MappingTarget Leave leave);

    /**
     * Convert paginated leaves to pagination response
     */
    default PaginationResponse<LeaveResponse> toPaginationResponse(Page<Leave> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}