package com.emenu.features.notification.dto.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramChat {
    private Long id;
    private String type;
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("last_name")
    private String lastName;
    
    public boolean isPrivate() {
        return "private".equals(type);
    }
}