package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.hr.dto.response.WorkScheduleResponse;
import com.emenu.features.hr.dto.update.WorkScheduleUpdateRequest;
import com.emenu.features.hr.models.WorkSchedule;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PaginationMapper.class}, unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface WorkScheduleMapper {

    @Mapping(target = "userInfo.id", source = "user.id")
    @Mapping(target = "userInfo.firstName", source = "user.firstName")
    @Mapping(target = "userInfo.lastName", source = "user.lastName")
    @Mapping(target = "userInfo.email", source = "user.email")
    @Mapping(target = "userInfo.phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "userInfo.profileImageUrl", source = "user.profileImageUrl")
    WorkScheduleResponse toResponse(WorkSchedule workSchedule);

    List<WorkScheduleResponse> toResponseList(List<WorkSchedule> workSchedules);

    WorkSchedule toEntity(WorkScheduleCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(WorkScheduleUpdateRequest request, @MappingTarget WorkSchedule workSchedule);

    /**
     * Convert paginated work schedules to pagination response
     */
    default PaginationResponse<WorkScheduleResponse> toPaginationResponse(Page<WorkSchedule> page, PaginationMapper paginationMapper) {
        return paginationMapper.toPaginationResponse(page, this::toResponseList);
    }
}
