package com.menghor.ksit.feature.master.dto.update;

import com.menghor.ksit.enumations.Status;
import lombok.Data;

@Data
public class RoomUpdateDto {
    private String name;
    private Status status = Status.ACTIVE;
}
