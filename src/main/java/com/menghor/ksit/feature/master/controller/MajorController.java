package com.menghor.ksit.feature.master.controller;


import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.master.dto.major.request.MajorFilterDto;
import com.menghor.ksit.feature.master.dto.major.request.MajorRequestDto;
import com.menghor.ksit.feature.master.dto.major.response.MajorResponseDto;
import com.menghor.ksit.feature.master.dto.major.response.MajorResponseListDto;
import com.menghor.ksit.feature.master.service.MajorService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/majors")
public class MajorController {
    private final MajorService majorService;

    @PostMapping
    public ApiResponse<MajorResponseDto> createMajor(@Valid @RequestBody MajorRequestDto majorRequestDto) {
        MajorResponseDto majorResponseDto = majorService.createMajor(majorRequestDto);
        return new ApiResponse<>(
                "Success",
                "Major created successfully...!",
                majorResponseDto
        );
    }

    @GetMapping("/getMajorById/{id}")
    public ApiResponse<MajorResponseDto> getMajorById(@PathVariable Long id) {
        MajorResponseDto majorResponseDto = majorService.getMajorById(id);
        return new ApiResponse<>(
                "Success",
                "Get major by id "+ id + " successfully...!",
                majorResponseDto
        );
    }

    @PostMapping("/updateById/{id}")
    public ApiResponse<MajorResponseDto> updateMajorById(@PathVariable Long id, @Valid @RequestBody MajorRequestDto majorRequestDto) {
        MajorResponseDto majorResponseDto = majorService.updateMajorById(id, majorRequestDto);
        return new ApiResponse<>(
                "Success",
                "Update major by id " + id + " successfully...!",
                majorResponseDto
        );
    }

    @DeleteMapping("/deleteById/{id}")
    public ApiResponse<MajorResponseDto> deleteMajorById(@PathVariable Long id) {
        MajorResponseDto majorResponseDto = majorService.deleteMajorById(id);
        return new ApiResponse<>(
                "Success",
                "Delete major by id " + id + " successfully...!",
                majorResponseDto
        );
    }

    @PostMapping("/getAllMajors")
    public ApiResponse<CustomPaginationResponseDto<MajorResponseListDto>> getAllMajors(@RequestBody MajorFilterDto filterDto) {
        CustomPaginationResponseDto<MajorResponseListDto> majorResponseDto = majorService.getAllMajors(filterDto);
        return new ApiResponse<>(
                "Success",
                "All majors fetched successfully...!",
                majorResponseDto
        );
    }
}
