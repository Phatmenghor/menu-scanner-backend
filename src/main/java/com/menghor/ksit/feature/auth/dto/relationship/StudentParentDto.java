package com.menghor.ksit.feature.auth.dto.relationship;

import com.menghor.ksit.enumations.ParentEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentParentDto {
    private Long id; // Optional, used for updates
    private String name; // ឈ្មោះឪពុកម្តាយ
    private String phone; // លេខទូរស័ព្ទ
    private String job; // មុខរបរ
    private String address; // អាសយដ្ឋាន
    private String age; // អាយុ
    private ParentEnum parentType; // ប្រភេទឪពុកម្តាយ
}