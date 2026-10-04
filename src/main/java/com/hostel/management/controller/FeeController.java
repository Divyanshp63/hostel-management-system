package com.hostel.management.controller;

import com.hostel.management.dto.request.BulkFeeGenerationRequest;
import com.hostel.management.dto.request.CreateFeeRequest;
import com.hostel.management.dto.request.UpdateFeeRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.FeeResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.enums.FeeStatus;
import com.hostel.management.service.FeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<FeeResponse>> createFee(
            @Valid @RequestBody CreateFeeRequest request
    ) {
        FeeResponse response = feeService.createFee(request);
        return new ResponseEntity<>(
                ApiResponse.success("Fee invoice created successfully", response),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<FeeResponse>>> generateBulkFees(
            @Valid @RequestBody BulkFeeGenerationRequest request
    ) {
        List<FeeResponse> response = feeService.generateBulkFees(request);
        return new ResponseEntity<>(
                ApiResponse.success("Bulk fees generated for all active residents", response),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<FeeResponse>> updateFee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFeeRequest request
    ) {
        FeeResponse response = feeService.updateFee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Fee invoice updated successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<PageResponse<FeeResponse>>> getAllFees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) FeeStatus status,
            @RequestParam(required = false) String month
    ) {
        PageResponse<FeeResponse> response = feeService.getAllFees(page, size, sortBy, sortDir, search, status, month);
        return ResponseEntity.ok(ApiResponse.success("Fees retrieved successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<FeeResponse>>> getMyFees(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<FeeResponse> response = feeService.getMyFees(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Your fee invoices retrieved successfully", response));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<FeeResponse>>> getFeesByStudentId(@PathVariable Long studentId) {
        List<FeeResponse> response = feeService.getFeesByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success("Student fee invoices retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    public ResponseEntity<ApiResponse<FeeResponse>> getFeeById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        FeeResponse response = feeService.getFeeById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Fee invoice details retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteFee(@PathVariable Long id) {
        feeService.deleteFee(id);
        return ResponseEntity.ok(ApiResponse.success("Fee invoice deleted successfully", null));
    }
}
