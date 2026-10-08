package com.hostel.management.controller;

import com.hostel.management.dto.request.CreateVisitorRequest;
import com.hostel.management.dto.request.VisitorApprovalRequest;
import com.hostel.management.dto.request.VisitorRejectionRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.VisitorResponse;
import com.hostel.management.enums.VisitorStatus;
import com.hostel.management.service.VisitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/visitors")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<VisitorResponse>> requestVisitorPass(
            @Valid @RequestBody CreateVisitorRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        VisitorResponse response = visitorService.requestVisitorPass(request, userDetails.getUsername());
        return new ResponseEntity<>(
                ApiResponse.success("Visitor pass request submitted. Awaiting admin approval.", response),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<VisitorResponse>> approveVisitorRequest(
            @PathVariable Long id,
            @RequestBody(required = false) VisitorApprovalRequest request
    ) {
        VisitorApprovalRequest approvalReq = request != null ? request : new VisitorApprovalRequest();
        VisitorResponse response = visitorService.approveVisitorRequest(id, approvalReq);
        return ResponseEntity.ok(ApiResponse.success("Visitor pass approved and check-in logged", response));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<VisitorResponse>> rejectVisitorRequest(
            @PathVariable Long id,
            @Valid @RequestBody VisitorRejectionRequest request
    ) {
        VisitorResponse response = visitorService.rejectVisitorRequest(id, request);
        return ResponseEntity.ok(ApiResponse.success("Visitor pass rejected", response));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<VisitorResponse>> completeVisit(@PathVariable Long id) {
        VisitorResponse response = visitorService.completeVisit(id);
        return ResponseEntity.ok(ApiResponse.success("Visitor check-out logged and visit marked as completed", response));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT', 'WARDEN')")
    public ResponseEntity<ApiResponse<VisitorResponse>> cancelVisitorRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        VisitorResponse response = visitorService.cancelVisitorRequest(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Visitor request cancelled successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<PageResponse<VisitorResponse>>> getAllVisitorRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "visitDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) VisitorStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate
    ) {
        PageResponse<VisitorResponse> response = visitorService.getAllVisitorRequests(
                page, size, sortBy, sortDir, search, status, visitDate
        );
        return ResponseEntity.ok(ApiResponse.success("Visitor requests retrieved successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<VisitorResponse>>> getMyVisitorRequests(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<VisitorResponse> response = visitorService.getMyVisitorRequests(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Your visitor passes retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN', 'STUDENT')")
    public ResponseEntity<ApiResponse<VisitorResponse>> getVisitorRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        VisitorResponse response = visitorService.getVisitorRequestById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Visitor pass details retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<String>> deleteVisitorRequest(@PathVariable Long id) {
        visitorService.deleteVisitorRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Visitor record deleted successfully", null));
    }
}
