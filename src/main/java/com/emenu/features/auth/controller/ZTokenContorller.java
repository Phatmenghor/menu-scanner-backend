package com.emenu.features.auth.controller;

import com.emenu.features.auth.dto.filter.BusinessFilterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
@Slf4j
public class ZTokenContorller {

    @PostMapping("")
    public ResponseEntity<String> getMyToken() {
        log.info("Get my token");
        return ResponseEntity.ok("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwaGF0bWVuZ2hvcjE5QGdtYWlsLmNvbSIsInJvbGVzIjoiUk9MRV9QTEFURk9STV9PV05FUiIsImlhdCI6MTc2NDgyMjAxOCwiZXhwIjoxMDAwMDE3NjQ4MjIwMTh9.8cFubNvLItiFIjpthDDnmuc7V0A_rL_UdiUsJbeZjPEGB2HUT5lYTMJHiZrfhXXj3IIXa562tUzNjpAwvKmxYw");
    }
}
