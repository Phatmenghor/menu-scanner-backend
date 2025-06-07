package com.menghor.ksit.feature.survey.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingOptionDto {
    private Integer value;
    private String label;
}