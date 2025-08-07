package com.emenu.features.notification.dto.response;

import lombok.Data;

@Data
public class TelegramMessageResponse {
    private Boolean ok;
    private Result result;
    private String description;
    private Integer errorCode;
    
    @Data
    public static class Result {
        private Long messageId;
        private User from;
        private Chat chat;
        private Long date;
        private String text;
        
        @Data
        public static class User {
            private Long id;
            private String firstName;
            private String lastName;
            private String username;
            private Boolean isBot;
        }
        
        @Data
        public static class Chat {
            private Long id;
            private String type;
            private String title;
            private String username;
            private String firstName;
            private String lastName;
        }
    }
}