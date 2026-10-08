package com.hostel.management.controller;

import com.hostel.management.dto.request.AdminAllocationRequest;
import com.hostel.management.dto.request.ApprovalRequest;
import com.hostel.management.dto.request.RejectionRequest;
import com.hostel.management.dto.request.RoomAllocationRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.RoomAllocationResponse;
import com.hostel.management.enums.AllocationStatus;
import com.hostel.management.service.RoomAllocationService;
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
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
public class RoomAllocationController {

    private final RoomAllocationService roomAllocationService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<RoomAllocationResponse>> requestAllocation(
            @Valid @RequestBody RoomAllocationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        RoomAllocationResponse response = roomAllocationService.requestAllocation(request, userDetails.getUsername());
        return new ResponseEntity<>(
                ApiResponse.success("Room allocation requested successfully. Awaiting warden approval.", response),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/direct")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<RoomAllocationResponse>> directAllocate(
            @Valid @RequestBody AdminAllocationRequest request
    ) {
        RoomAllocationResponse response = roomAllocationService.directAllocate(request);
        return new ResponseEntity<>(
                ApiResponse.success("Room allocated directly by Warden", response),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<RoomAllocationResponse>> approveAllocation(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request
    ) {
        RoomAllocationResponse response = roomAllocationService.approveAllocation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Room allocation approved and bed assigned", response));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<RoomAllocationResponse>> rejectAllocation(
            @PathVariable Long id,
            @Valid @RequestBody RejectionRequest request
    ) {
        RoomAllocationResponse response = roomAllocationService.rejectAllocation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Room allocation request rejected", response));
    }

    @PutMapping("/{id}/vacate")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<RoomAllocationResponse>> vacateAllocation(@PathVariable Long id) {
        RoomAllocationResponse response = roomAllocationService.vacateAllocation(id);
        return ResponseEntity.ok(ApiResponse.success("Room vacated successfully and bed released", response));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT', 'WARDEN')")
    public ResponseEntity<ApiResponse<RoomAllocationResponse>> cancelAllocation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        RoomAllocationResponse response = roomAllocationService.cancelAllocationRequest(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Allocation request cancelled successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('WARDEN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<PageResponse<RoomAllocationResponse>>> getAllAllocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) AllocationStatus status
    ) {
        PageResponse<RoomAllocationResponse> response = roomAllocationService.getAllAllocations(
                page, size, sortBy, sortDir, search, status
        );
        return ResponseEntity.ok(ApiResponse.success("Allocations retrieved successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<RoomAllocationResponse>>> getMyAllocations(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<RoomAllocationResponse> response = roomAllocationService.getMyAllocations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Your allocation history retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN', 'ACCOUNTANT', 'STUDENT')")
    public ResponseEntity<ApiResponse<RoomAllocationResponse>> getAllocationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        RoomAllocationResponse response = roomAllocationService.getAllocationById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Allocation details retrieved successfully", response));
    }
}
