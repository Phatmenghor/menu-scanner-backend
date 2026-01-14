package com.emenu.features.leave.controller;

import com.emenu.features.leave.dto.request.LeavePolicyCreateRequest;
import com.emenu.features.leave.dto.response.LeavePolicyResponse;
import com.emenu.features.leave.dto.update.LeavePolicyUpdateRequest;
import com.emenu.features.leave.service.LeavePolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-policies")
@RequiredArgsConstructor
public class LeavePolicyController {

    private final LeavePolicyService policyService;

    @PostMapping
    public ResponseEntity<LeavePolicyResponse> createPolicy(@Valid @RequestBody LeavePolicyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.createPolicy(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeavePolicyResponse> updatePolicy(
            @PathVariable Long id,
            @Valid @RequestBody LeavePolicyUpdateRequest request) {
        return ResponseEntity.ok(policyService.updatePolicy(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeavePolicyResponse> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<LeavePolicyResponse>> getPoliciesByBusinessId(@PathVariable Long businessId) {
        return ResponseEntity.ok(policyService.getPoliciesByBusinessId(businessId));
    }

    @GetMapping
    public ResponseEntity<Page<LeavePolicyResponse>> getAllPolicies(Pageable pageable) {
        return ResponseEntity.ok(policyService.getAllPolicies(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}
