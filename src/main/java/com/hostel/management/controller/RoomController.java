package com.hostel.management.controller;

import com.hostel.management.dto.request.CreateRoomRequest;
import com.hostel.management.dto.request.UpdateRoomRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.RoomResponse;
import com.hostel.management.enums.RoomStatus;
import com.hostel.management.enums.RoomType;
import com.hostel.management.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody CreateRoomRequest request
    ) {
        RoomResponse response = roomService.createRoom(request);
        return new ResponseEntity<>(
                ApiResponse.success("Room created successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    public ResponseEntity<ApiResponse<PageResponse<RoomResponse>>> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "roomNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) RoomType roomType
    ) {
        PageResponse<RoomResponse> response = roomService.getAllRooms(
                page, size, sortBy, sortDir, search, status, roomType
        );
        return ResponseEntity.ok(ApiResponse.success("Rooms retrieved successfully", response));
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAvailableRooms() {
        List<RoomResponse> response = roomService.getAvailableRooms();
        return ResponseEntity.ok(ApiResponse.success("Available rooms retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        RoomResponse response = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success("Room details retrieved successfully", response));
    }

    @GetMapping("/number/{roomNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomByNumber(@PathVariable String roomNumber) {
        RoomResponse response = roomService.getRoomByNumber(roomNumber);
        return ResponseEntity.ok(ApiResponse.success("Room details retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomRequest request
    ) {
        RoomResponse response = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully", null));
    }
}
