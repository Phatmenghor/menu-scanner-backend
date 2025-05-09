package com.menghor.ksit.feature.master.dto.request;

import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoomRequestDto {

    @NotBlank(message = "Room name is required")
    private String name;

    private Status status = Status.ACTIVE;
}
