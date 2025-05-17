package com.menghor.ksit.utils.QR;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.util.Base64;


@Component
@Slf4j
public class QrCodeGenerator {

    /**
     * Generate a Base64 encoded QR code image from the given content.
     * 
     * @param content The content to encode in the QR code
     * @param width QR code width in pixels
     * @param height QR code height in pixels
     * @return Base64 encoded string of the QR code image
     */
    public String generateQrCodeBase64(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            throw new RuntimeException("Error generating QR code", e);
        }
    }
}