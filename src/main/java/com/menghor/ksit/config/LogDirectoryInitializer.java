package com.menghor.ksit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class LogDirectoryInitializer {

    @EventListener(ApplicationReadyEvent.class)
    public void initializeLogDirectory() {
        String logPath = System.getProperty("LOG_PATH", "/opt/logs");  // Changed default

        try {
            Path logDir = Paths.get(logPath);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                log.info("✅ Created log directory: {}", logDir.toAbsolutePath());
            } else {
                log.info("✅ Log directory already exists: {}", logDir.toAbsolutePath());
            }

            // Check if directory is writable
            File logDirFile = logDir.toFile();
            if (!logDirFile.canWrite()) {
                log.warn("⚠️ Log directory is not writable: {}", logDir.toAbsolutePath());
            } else {
                log.info("✅ Log directory is writable: {}", logDir.toAbsolutePath());
            }

        } catch (Exception e) {
            log.error("❌ Failed to create log directory: {}", logPath, e);
        }
    }
}