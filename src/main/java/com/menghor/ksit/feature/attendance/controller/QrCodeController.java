package com.menghor.ksit.feature.attendance.controller;

import com.menghor.ksit.feature.attendance.service.AttendanceSessionService;
import com.menghor.ksit.utils.QR.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
@RequestMapping("/api/v1/qr-code")
@RequiredArgsConstructor
public class QrCodeController {

    private final AttendanceSessionService sessionService;
    private final QrCodeGenerator qrCodeGenerator;
    
    @GetMapping(value = "/{sessionId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCodeImage(@PathVariable Long sessionId) {
        String qrCode = sessionService.generateQrCode(sessionId).getQrCode();
        String base64QrCode = qrCodeGenerator.generateQrCodeBase64(qrCode, 250, 250);
        byte[] qrCodeImage = Base64.getDecoder().decode(base64QrCode);
        
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCodeImage);
    }
}
