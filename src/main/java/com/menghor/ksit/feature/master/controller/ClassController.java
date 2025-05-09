package com.menghor.ksit.feature.master.controller;


import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.master.dto.filter.ClassFilterDto;
import com.menghor.ksit.feature.master.dto.request.ClassRequestDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseListDto;
import com.menghor.ksit.feature.master.dto.update.ClassUpdateDto;
import com.menghor.ksit.feature.master.service.ClassService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/classes")
public class ClassController {
    private final ClassService classService;

    @PostMapping
    public ApiResponse<ClassResponseDto> create(@Valid @RequestBody ClassRequestDto classRequestDto) {
        ClassResponseDto classResponseDto = classService.createClass(classRequestDto);
        return new ApiResponse<>(
                "Success",
                "Classes created successfully...!",
                classResponseDto
        );
    }

    @GetMapping("/getClassById/{id}")
    public ApiResponse<ClassResponseDto> getClassById(@PathVariable Long id) {
        ClassResponseDto classResponseDto = classService.getClassById(id);
        return new ApiResponse<>(
                "Success",
                "Get class by id "+ id + " successfully...!",
                classResponseDto
        );
    }

    @PostMapping("/updateById/{id}")
    public ApiResponse<ClassResponseDto> updateById(@PathVariable Long id, @Valid @RequestBody ClassUpdateDto classRequestDto) {
        ClassResponseDto classResponseDto = classService.updateClassById(id, classRequestDto);
        return new ApiResponse<>(
                "Success",
                "Update class by id " + id + " successfully...!",
                classResponseDto
        );
    }

    @DeleteMapping("/deleteById/{id}")
    public ApiResponse<ClassResponseDto> deleteById(@PathVariable Long id) {
        ClassResponseDto classResponseDto = classService.deleteClassById(id);
        return new ApiResponse<>(
                "Success",
                "Delete class by id " + id + " successfully...!",
                classResponseDto
        );
    }

    @PostMapping("/getAllClasses")
    public ApiResponse<CustomPaginationResponseDto<ClassResponseListDto>> getAllClasses(@RequestBody ClassFilterDto filterDto) {
        CustomPaginationResponseDto<ClassResponseListDto> classResponseList = classService.getAllClasses(filterDto);
        return new ApiResponse<>(
                "Success",
                "All classes fetched successfully...!",
                classResponseList
        );
    }
}
