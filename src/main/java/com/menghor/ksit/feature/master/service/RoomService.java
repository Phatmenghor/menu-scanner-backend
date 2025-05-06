package com.menghor.ksit.feature.master.service;

import com.menghor.ksit.feature.master.dto.room.request.RoomFilterDto;
import com.menghor.ksit.feature.master.dto.room.request.RoomRequestDto;
import com.menghor.ksit.feature.master.dto.room.response.RoomResponseDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface RoomService {

    RoomResponseDto createRoom(RoomRequestDto roomRequest);
    RoomResponseDto getRoomById(Long id);
    RoomResponseDto updateRoomById(RoomRequestDto roomRequest, Long id);
    RoomResponseDto deleteRoomById(Long id);
    CustomPaginationResponseDto<RoomResponseDto> getAllRoom(RoomFilterDto filterDto);
}
