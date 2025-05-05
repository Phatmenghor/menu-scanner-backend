package com.menghor.ksit.feature.master.dto.room.response;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomResponseDto {
    private Long id;
    private String name;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
