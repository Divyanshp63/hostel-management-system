package com.hostel.management.controller;

import com.hostel.management.dto.request.CreateLeaveRequest;
import com.hostel.management.dto.request.LeaveActionRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.LeaveResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.enums.LeaveStatus;
import com.hostel.management.enums.LeaveType;
import com.hostel.management.service.LeaveService;
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
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<LeaveResponse>> applyLeave(
            @Valid @RequestBody CreateLeaveRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        LeaveResponse response = leaveService.applyLeave(request, userDetails.getUsername());
        return new ResponseEntity<>(
                ApiResponse.success("Leave application submitted. Awaiting warden approval.", response),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<LeaveResponse>> approveLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveActionRequest request
    ) {
        LeaveActionRequest actionReq = request != null ? request : new LeaveActionRequest();
        LeaveResponse response = leaveService.approveLeave(id, actionReq);
        return ResponseEntity.ok(ApiResponse.success("Leave request approved successfully", response));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<LeaveResponse>> rejectLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveActionRequest request
    ) {
        LeaveActionRequest actionReq = request != null ? request : new LeaveActionRequest();
        LeaveResponse response = leaveService.rejectLeave(id, actionReq);
        return ResponseEntity.ok(ApiResponse.success("Leave request rejected", response));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT', 'WARDEN')")
    public ResponseEntity<ApiResponse<LeaveResponse>> cancelLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        LeaveResponse response = leaveService.cancelLeave(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Leave request cancelled", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<PageResponse<LeaveResponse>>> getAllLeaves(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) LeaveType leaveType
    ) {
        PageResponse<LeaveResponse> response = leaveService.getAllLeaves(
                page, size, sortBy, sortDir, search, status, leaveType
        );
        return ResponseEntity.ok(ApiResponse.success("Leave applications retrieved successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getMyLeaves(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<LeaveResponse> response = leaveService.getMyLeaves(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Your leave requests retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN', 'STUDENT')")
    public ResponseEntity<ApiResponse<LeaveResponse>> getLeaveById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        LeaveResponse response = leaveService.getLeaveById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Leave details retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<String>> deleteLeave(@PathVariable Long id) {
        leaveService.deleteLeave(id);
        return ResponseEntity.ok(ApiResponse.success("Leave record deleted successfully", null));
    }
}
