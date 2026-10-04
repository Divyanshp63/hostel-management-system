package com.hostel.management.controller;

import com.hostel.management.dto.request.CreateStudentRequest;
import com.hostel.management.dto.request.UpdateStudentRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.StudentResponse;
import com.hostel.management.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @Valid @RequestBody CreateStudentRequest request
    ) {
        StudentResponse response = studentService.createStudent(request);
        return new ResponseEntity<>(
                ApiResponse.success("Student profile created successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<PageResponse<StudentResponse>>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "") String search
    ) {
        PageResponse<StudentResponse> response = studentService.getAllStudents(page, size, sortBy, sortDir, search);
        return ResponseEntity.ok(ApiResponse.success("Students retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable Long id) {
        StudentResponse response = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success("Student details retrieved successfully", response));
    }

    @GetMapping("/profile/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<StudentResponse>> getMyStudentProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        StudentResponse response = studentService.getStudentByCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Your student profile retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        StudentResponse response = studentService.updateStudent(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Student profile updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.success("Student and associated user account deleted successfully", null));
    }
}
