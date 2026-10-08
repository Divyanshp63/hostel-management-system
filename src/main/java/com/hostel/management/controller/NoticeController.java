package com.hostel.management.controller;

import com.hostel.management.dto.request.CreateNoticeRequest;
import com.hostel.management.dto.request.UpdateNoticeRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.NoticeResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.enums.NoticePriority;
import com.hostel.management.enums.NoticeTarget;
import com.hostel.management.service.NoticeService;
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
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(
            @Valid @RequestBody CreateNoticeRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        NoticeResponse response = noticeService.createNotice(request, userDetails.getUsername());
        return new ResponseEntity<>(
                ApiResponse.success("Notice published successfully", response),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoticeRequest request
    ) {
        NoticeResponse response = noticeService.updateNotice(id, request);
        return ResponseEntity.ok(ApiResponse.success("Notice updated successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('WARDEN', 'STUDENT', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<PageResponse<NoticeResponse>>> getAllNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) NoticePriority priority,
            @RequestParam(required = false) NoticeTarget target,
            @RequestParam(required = false) Boolean active
    ) {
        PageResponse<NoticeResponse> response = noticeService.getAllNotices(
                page, size, sortBy, sortDir, search, priority, target, active
        );
        return ResponseEntity.ok(ApiResponse.success("Notices retrieved successfully", response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('WARDEN', 'STUDENT', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> getActiveNotices() {
        List<NoticeResponse> response = noticeService.getActiveNotices();
        return ResponseEntity.ok(ApiResponse.success("Active notices retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN', 'STUDENT', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNoticeById(@PathVariable Long id) {
        NoticeResponse response = noticeService.getNoticeById(id);
        return ResponseEntity.ok(ApiResponse.success("Notice details retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<String>> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success("Notice deleted successfully", null));
    }
}
