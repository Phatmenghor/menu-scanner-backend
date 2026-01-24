package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.hr.dto.response.WorkScheduleResponse;
import com.emenu.features.hr.dto.update.WorkScheduleUpdateRequest;
import com.emenu.features.hr.models.WorkSchedule;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class})
public interface WorkScheduleMapper {

    @Mapping(target = "userInfo.id", source = "user.id")
    @Mapping(target = "userInfo.userIdentifier", source = "user.userIdentifier")
    @Mapping(target = "userInfo.fullName", expression = "java(workSchedule.getUser() != null ? workSchedule.getUser().getFullName() : null)")
    @Mapping(target = "userInfo.email", source = "user.email")
    @Mapping(target = "userInfo.profileImageUrl", source = "user.profileImageUrl")
    WorkScheduleResponse toResponse(WorkSchedule workSchedule);

    List<WorkScheduleResponse> toResponseList(List<WorkSchedule> workSchedules);

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
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "business", ignore = true)
    @Mapping(target = "scheduleTypeEnum", source = "scheduleTypeEnumName")
    WorkSchedule toEntity(WorkScheduleCreateRequest request);

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
    @Mapping(target = "scheduleTypeEnum", ignore = true)
    void updateEntity(WorkScheduleUpdateRequest request, @MappingTarget WorkSchedule workSchedule);

    /**
     * Convert paginated work schedules to pagination response
     */
    default PaginationResponse<WorkScheduleResponse> toPaginationResponse(Page<WorkSchedule> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}