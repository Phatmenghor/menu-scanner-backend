package com.menghor.ksit.feature.school.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.school.dto.filter.RequestFilterDto;
import com.menghor.ksit.feature.school.dto.request.RequestCreateDto;
import com.menghor.ksit.feature.school.dto.response.RequestResponseDto;
import com.menghor.ksit.feature.school.dto.update.RequestUpdateDto;
import com.menghor.ksit.feature.school.service.RequestService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RequestController {
    
    private final RequestService requestService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RequestResponseDto> createRequest(
            @Valid @RequestBody RequestCreateDto createDto) {
        log.info("Creating new request with title: {}", createDto.getTitle());
        RequestResponseDto response = requestService.createRequest(createDto);
        log.info("Request created successfully with ID: {}", response.getId());
        return ApiResponse.success("Request created successfully", response);
    }
    
    @PutMapping("/{id}")
    public ApiResponse<RequestResponseDto> updateRequest(
            @PathVariable Long id,
            @Valid @RequestBody RequestUpdateDto updateDto) {
        log.info("Updating request with ID: {}", id);
        RequestResponseDto response = requestService.updateRequest(id, updateDto);
        log.info("Request updated successfully with ID: {}", id);
        return ApiResponse.success("Request updated successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<RequestResponseDto> getRequestById(
            @Parameter(description = "Request ID") @PathVariable Long id) {
        log.info("Fetching request with ID: {}", id);
        RequestResponseDto response = requestService.getRequestById(id);
        log.info("Request fetched successfully with ID: {}", id);
        return ApiResponse.success("Request fetched successfully", response);
    }
    
    @PostMapping("/all")
    public ApiResponse<CustomPaginationResponseDto<RequestResponseDto>> getAllRequests(
            @RequestBody RequestFilterDto filterDto) {
        log.info("Fetching all requests with filter: {}", filterDto);
        CustomPaginationResponseDto<RequestResponseDto> response = requestService.getAllRequests(filterDto);
        log.info("Requests fetched successfully. Total elements: {}", response.getTotalElements());
        return ApiResponse.success("Requests fetched successfully", response);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<RequestResponseDto> deleteRequest(
            @Parameter(description = "Request ID") @PathVariable Long id) {
        log.info("Deleting request with ID: {}", id);
        RequestResponseDto response = requestService.deleteRequest(id);
        log.info("Request deleted successfully with ID: {}", id);
        return ApiResponse.success("Request deleted successfully", response);
    }
}
