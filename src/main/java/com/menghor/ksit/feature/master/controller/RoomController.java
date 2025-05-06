package com.menghor.ksit.feature.master.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.master.dto.room.request.RoomFilterDto;
import com.menghor.ksit.feature.master.dto.room.request.RoomRequestDto;
import com.menghor.ksit.feature.master.dto.room.response.RoomResponseDto;
import com.menghor.ksit.feature.master.service.RoomService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/rooms")
public class RoomController {
    private final RoomService roomService;

    @PostMapping
    public ApiResponse<RoomResponseDto> create(@Valid @RequestBody RoomRequestDto roomRequestDto) {
        RoomResponseDto roomResponseDto = roomService.createRoom(roomRequestDto);
        return new ApiResponse<>(
                "Success",
                "Room created successfully...!"
                , roomResponseDto
        );
    }

    @PostMapping("/getById/{id}")
    public ApiResponse<RoomResponseDto> getById(@PathVariable Long id) {
        RoomResponseDto roomResponseDto = roomService.getRoomById(id);
        return new ApiResponse<>(
                "Success",
                "Get room by id " + id + " successfully...!",
                roomResponseDto
        );
    }

    @PostMapping("/updateById/{id}")
    public ApiResponse<RoomResponseDto> updateById(@Valid @RequestBody RoomRequestDto roomRequest, @PathVariable Long id) {
        RoomResponseDto roomResponseDto = roomService.updateRoomById(roomRequest, id);
        return new ApiResponse<>(
                "Success",
                "Update room by id " + id + " successfully...!"
                , roomResponseDto
        );
    }

    @PostMapping("/deleteById/{id}")
    public ApiResponse<RoomResponseDto> deleteById(@PathVariable Long id) {
        RoomResponseDto roomResponseDto = roomService.deleteRoomById(id);
        return new ApiResponse<>(
                "Success",
                "Delete room by id " + id + " successfully...!"
                , roomResponseDto
        );
    }

    @PostMapping("/getAllRooms")
    public ApiResponse<CustomPaginationResponseDto<RoomResponseDto>> getAllRooms(@RequestBody RoomFilterDto filterDto) {
        log.info("Get all room {}", filterDto);
        CustomPaginationResponseDto<RoomResponseDto> allRooms = roomService.getAllRoom(filterDto);
        return new ApiResponse<>(
                "Success",
                "All rooms fetched successfully...!"
                , allRooms
        );
    }
}
