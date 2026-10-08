package com.hostel.management.controller;

import com.hostel.management.dto.request.MessMenuRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.MessMenuResponse;
import com.hostel.management.dto.response.WeeklyMenuResponse;
import com.hostel.management.service.MessMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;

@RestController
@RequestMapping("/api/mess-menu")
@RequiredArgsConstructor
public class MessMenuController {

    private final MessMenuService messMenuService;

    @PostMapping
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<MessMenuResponse>> createMenu(
            @Valid @RequestBody MessMenuRequest request) {
        MessMenuResponse response = messMenuService.createMenu(request);
        return new ResponseEntity<>(
                ApiResponse.success("Mess menu created successfully", response),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/upsert")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<MessMenuResponse>> createOrUpdateMenu(
            @Valid @RequestBody MessMenuRequest request) {
        MessMenuResponse response = messMenuService.createOrUpdateMenu(request);
        return ResponseEntity.ok(
                ApiResponse.success("Mess menu saved successfully", response)
        );
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<List<MessMenuResponse>>> bulkSaveMenu(
            @Valid @RequestBody List<MessMenuRequest> requests) {
        List<MessMenuResponse> responses = messMenuService.bulkSaveMenu(requests);
        return ResponseEntity.ok(
                ApiResponse.success("Bulk mess menu updated successfully", responses)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<MessMenuResponse>> updateMenu(
            @PathVariable Long id,
            @Valid @RequestBody MessMenuRequest request) {
        MessMenuResponse response = messMenuService.updateMenu(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Mess menu updated successfully", response)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('WARDEN')")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(@PathVariable Long id) {
        messMenuService.deleteMenu(id);
        return ResponseEntity.ok(
                ApiResponse.success("Mess menu deleted successfully", null)
        );
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('WARDEN', 'STUDENT', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<MessMenuResponse>>> getTodayMenu() {
        List<MessMenuResponse> response = messMenuService.getTodayMenu();
        return ResponseEntity.ok(
                ApiResponse.success("Today's mess menu fetched successfully", response)
        );
    }

    @GetMapping("/day/{dayOfWeek}")
    @PreAuthorize("hasAnyRole('WARDEN', 'STUDENT', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<MessMenuResponse>>> getMenuForDay(
            @PathVariable DayOfWeek dayOfWeek) {
        List<MessMenuResponse> response = messMenuService.getMenuForDay(dayOfWeek);
        return ResponseEntity.ok(
                ApiResponse.success("Mess menu for " + dayOfWeek + " fetched successfully", response)
        );
    }

    @GetMapping("/weekly")
    @PreAuthorize("hasAnyRole('WARDEN', 'STUDENT', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<WeeklyMenuResponse>> getWeeklyMenu() {
        WeeklyMenuResponse response = messMenuService.getWeeklyMenu();
        return ResponseEntity.ok(
                ApiResponse.success("Weekly mess schedule fetched successfully", response)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WARDEN', 'STUDENT', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<MessMenuResponse>> getMenuById(@PathVariable Long id) {
        MessMenuResponse response = messMenuService.getMenuById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Mess menu retrieved successfully", response)
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('WARDEN', 'STUDENT', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<MessMenuResponse>>> getAllMenus() {
        List<MessMenuResponse> response = messMenuService.getAllMenus();
        return ResponseEntity.ok(
                ApiResponse.success("All mess menu records fetched successfully", response)
        );
    }
}
