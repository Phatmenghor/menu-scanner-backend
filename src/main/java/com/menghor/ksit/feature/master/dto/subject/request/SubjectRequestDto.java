package com.menghor.ksit.feature.master.dto.subject.request;

import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubjectRequestDto {

    @NotBlank(message = "Subject name is required")
    private String name;

    private Status status = Status.ACTIVE;
}
