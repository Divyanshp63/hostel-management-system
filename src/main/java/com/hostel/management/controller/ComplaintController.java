package com.hostel.management.controller;

import com.hostel.management.dto.request.CreateComplaintRequest;
import com.hostel.management.dto.request.UpdateComplaintStatusRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.ComplaintResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.ComplaintStatus;
import com.hostel.management.service.ComplaintService;
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
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
            @Valid @RequestBody CreateComplaintRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ComplaintResponse response = complaintService.createComplaint(request, userDetails.getUsername());
        return new ResponseEntity<>(
                ApiResponse.success("Complaint registered successfully", response),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('COMPLAINT_STAFF')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> acceptComplaint(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ComplaintResponse response = complaintService.acceptComplaint(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Complaint ticket accepted and moved to IN_PROGRESS", response));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('COMPLAINT_STAFF')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> resolveComplaint(
            @PathVariable Long id,
            @RequestParam String resolutionRemarks,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ComplaintResponse response = complaintService.resolveComplaint(id, resolutionRemarks, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Complaint marked as RESOLVED_BY_MAINTENANCE", response));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> verifyComplaint(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ComplaintResponse response = complaintService.verifyComplaintByWarden(id, remarks, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Complaint verified and CLOSED by Warden", response));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> returnComplaint(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ComplaintResponse response = complaintService.returnComplaintByWarden(id, reason, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Complaint returned to Maintenance Department", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('WARDEN', 'COMPLAINT_STAFF')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComplaintStatusRequest request
    ) {
        ComplaintResponse response = complaintService.updateComplaintStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('WARDEN', 'COMPLAINT_STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<ComplaintResponse>>> getAllComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) ComplaintCategory category
    ) {
        PageResponse<ComplaintResponse> response = complaintService.getAllComplaints(
                page, size, sortBy, sortDir, search, status, category
        );
        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getMyComplaints(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ComplaintResponse> response = complaintService.getMyComplaints(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Your complaints retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN', 'STUDENT', 'COMPLAINT_STAFF')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ComplaintResponse response = complaintService.getComplaintById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Complaint details retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<String>> deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.ok(ApiResponse.success("Complaint deleted successfully", null));
    }
}
