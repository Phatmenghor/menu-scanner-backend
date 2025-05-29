package com.menghor.ksit.feature.school.dto.update;

import com.menghor.ksit.enumations.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestStatusUpdateDto {
    @NotNull(message = "Status is required")
    private RequestStatus status;
    
    private String staffComment;
}
