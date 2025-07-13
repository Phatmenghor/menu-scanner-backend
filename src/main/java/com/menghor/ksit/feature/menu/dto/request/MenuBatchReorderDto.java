package com.menghor.ksit.feature.menu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MenuBatchReorderDto {
    @NotNull(message = "Menu reorder list is required")
    private List<MenuReorderDto> menuReorders;
}