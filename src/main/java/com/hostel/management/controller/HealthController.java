package com.hostel.management.controller;

import com.hostel.management.dto.request.HealthCheckRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> checkHealth() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Hostel Management System API");
        status.put("version", "1.0.0");
        return ResponseEntity.ok(ApiResponse.success("System is up and running smoothly!", status));
    }

    @PostMapping("/validate-test")
    public ResponseEntity<ApiResponse<String>> testValidation(@Valid @RequestBody HealthCheckRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Validation passed successfully!", "Hello " + request.getName()));
    }

    @GetMapping("/test-error/{type}")
    public ResponseEntity<ApiResponse<String>> testException(@PathVariable String type) {
        if ("notfound".equalsIgnoreCase(type)) {
            throw new ResourceNotFoundException("Test resource was not found in the database");
        } else if ("badrequest".equalsIgnoreCase(type)) {
            throw new BadRequestException("Test bad request: Invalid business parameter supplied");
        }
        return ResponseEntity.ok(ApiResponse.success("No error triggered for type: " + type));
    }
}
