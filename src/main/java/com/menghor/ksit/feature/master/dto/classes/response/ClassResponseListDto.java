package com.menghor.ksit.feature.master.dto.classes.response;

import com.menghor.ksit.enumations.DegreeEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.YearLevelEnum;
import com.menghor.ksit.feature.master.dto.major.response.MajorResponseDto;
import lombok.Data;

@Data
public class ClassResponseListDto {
    private Long id;
    private String code;
    private String majorName;
    private Integer academyYear;
    private DegreeEnum degree;
    private YearLevelEnum yearLevel;
    private Status status;
}
