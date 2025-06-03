package com.menghor.ksit.feature.school.service;

import com.menghor.ksit.feature.school.dto.filter.RequestFilterDto;
import com.menghor.ksit.feature.school.dto.filter.RequestHistoryFilterDto;
import com.menghor.ksit.feature.school.dto.request.RequestCreateDto;
import com.menghor.ksit.feature.school.dto.response.RequestHistoryDto;
import com.menghor.ksit.feature.school.dto.response.RequestResponseDto;
import com.menghor.ksit.feature.school.dto.update.RequestUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface RequestService {
    
    RequestResponseDto createRequest(RequestCreateDto createDto);
    
    RequestResponseDto updateRequest(Long id, RequestUpdateDto updateDto);

    RequestResponseDto getRequestById(Long id);
    
    CustomPaginationResponseDto<RequestResponseDto> getAllRequests(RequestFilterDto filterDto);

    CustomPaginationResponseDto<RequestHistoryDto> getRequestHistory(RequestHistoryFilterDto filterDto);

    RequestHistoryDto getRequestHistoryDetail(Long historyId);

    RequestResponseDto deleteRequest(Long id);

}