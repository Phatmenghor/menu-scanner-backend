package com.emenu.features.attendance.controller;

import com.emenu.features.attendance.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.attendance.dto.response.WorkScheduleResponse;
import com.emenu.features.attendance.dto.update.WorkScheduleUpdateRequest;
import com.emenu.features.attendance.service.WorkScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/work-schedules")
@RequiredArgsConstructor
public class WorkScheduleController {

    private final WorkScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<WorkScheduleResponse> createSchedule(@Valid @RequestBody WorkScheduleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody WorkScheduleUpdateRequest request) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkScheduleResponse> getScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WorkScheduleResponse>> getSchedulesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByUserId(userId));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<WorkScheduleResponse>> getSchedulesByBusinessId(@PathVariable Long businessId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByBusinessId(businessId));
    }

    @GetMapping
    public ResponseEntity<Page<WorkScheduleResponse>> getAllSchedules(Pageable pageable) {
        return ResponseEntity.ok(scheduleService.getAllSchedules(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
