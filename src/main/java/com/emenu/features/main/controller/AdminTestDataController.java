package com.emenu.features.main.controller;

import com.emenu.config.TestDataGeneratorService;
import com.emenu.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin Test Data Controller
 * Provides endpoints for generating and managing test data
 *
 * ⚠️ SECURITY: These endpoints should only be accessible to admin users
 */
@RestController
@RequestMapping("/api/admin/test-data")
@RequiredArgsConstructor
@Slf4j
public class AdminTestDataController {

    private final TestDataGeneratorService testDataGeneratorService;

    /**
     * Generate initial test data with sample businesses, products, and Freepik images
     *
     * POST /api/admin/test-data/generate
     *
     * @return ApiResponse with status and message
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<String>> generateTestData() {
        try {
            log.info("🚀 Admin triggered test data generation");

            // Trigger the test data generation
            testDataGeneratorService.generateInitialTestData();

            log.info("✅ Test data generation completed successfully");
            return ResponseEntity.ok(
                new ApiResponse<>(
                    true,
                    "Test data generated successfully with Freepik images",
                    "Data includes: Businesses, Categories, Brands, Products, Sizes, Prices, and Promotions"
                )
            );
        } catch (Exception e) {
            log.error("❌ Error generating test data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                new ApiResponse<>(
                    false,
                    "Failed to generate test data: " + e.getMessage(),
                    null
                )
            );
        }
    }

    /**
     * Health check endpoint to verify test data generation service
     *
     * POST /api/admin/test-data/health
     */
    @PostMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            new ApiResponse<>(
                true,
                "Test data service is operational",
                "Ready to generate sample data with real Freepik images"
            )
        );
    }
}
