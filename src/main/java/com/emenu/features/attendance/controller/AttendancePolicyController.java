package com.emenu.features.attendance.controller;

import com.emenu.features.attendance.dto.request.AttendancePolicyCreateRequest;
import com.emenu.features.attendance.dto.response.AttendancePolicyResponse;
import com.emenu.features.attendance.dto.update.AttendancePolicyUpdateRequest;
import com.emenu.features.attendance.service.AttendancePolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance-policies")
@RequiredArgsConstructor
public class AttendancePolicyController {

    private final AttendancePolicyService policyService;

    @PostMapping
    public ResponseEntity<AttendancePolicyResponse> createPolicy(@Valid @RequestBody AttendancePolicyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.createPolicy(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendancePolicyResponse> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody AttendancePolicyUpdateRequest request) {
        return ResponseEntity.ok(policyService.updatePolicy(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendancePolicyResponse> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<AttendancePolicyResponse>> getPoliciesByBusinessId(@PathVariable Long businessId) {
        return ResponseEntity.ok(policyService.getPoliciesByBusinessId(businessId));
    }

    @GetMapping
    public ResponseEntity<Page<AttendancePolicyResponse>> getAllPolicies(Pageable pageable) {
        return ResponseEntity.ok(policyService.getAllPolicies(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}
