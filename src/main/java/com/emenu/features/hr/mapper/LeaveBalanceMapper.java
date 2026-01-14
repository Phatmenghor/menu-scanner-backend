package com.emenu.features.hr.mapper;

import com.emenu.features.hr.dto.response.LeaveBalanceResponse;
import com.emenu.features.hr.models.LeaveBalance;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LeaveBalanceMapper {
    LeaveBalanceResponse toResponse(LeaveBalance balance);
    List<LeaveBalanceResponse> toResponseList(List<LeaveBalance> balances);
}
