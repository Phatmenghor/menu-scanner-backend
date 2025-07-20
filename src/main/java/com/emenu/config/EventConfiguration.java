package com.emenu.config;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventConfiguration {

    @Bean
    public ApplicationEventPublisher applicationEventPublisher() {
        return new org.springframework.context.event.SimpleApplicationEventPublisher();
    }
}