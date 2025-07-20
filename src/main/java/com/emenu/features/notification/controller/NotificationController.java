package com.emenu.features.notification.controller;

import com.emenu.features.notification.service.EmailService;
import com.emenu.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/email/send")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendCustomEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String templateName,
            @RequestBody Map<String, Object> variables) {
        log.info("Sending custom email to: {}", to);
        emailService.sendCustomEmail(to, subject, templateName, variables);
        return ResponseEntity.ok(ApiResponse.success("Email sent successfully", null));
    }

    @PostMapping("/email/send-plain")
    @PreAuthorize("hasRole('PLATFORM_OWNER') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendPlainEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content) {
        log.info("Sending plain email to: {}", to);
        emailService.sendPlainEmail(to, subject, content);
        return ResponseEntity.ok(ApiResponse.success("Email sent successfully", null));
    }
}
