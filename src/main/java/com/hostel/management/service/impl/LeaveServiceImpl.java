package com.hostel.management.service.impl;

import com.hostel.management.dto.request.CreateLeaveRequest;
import com.hostel.management.dto.request.LeaveActionRequest;
import com.hostel.management.dto.response.LeaveResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.entity.LeaveRequest;
import com.hostel.management.entity.RoomAllocation;
import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.enums.AllocationStatus;
import com.hostel.management.enums.LeaveStatus;
import com.hostel.management.enums.LeaveType;
import com.hostel.management.enums.Role;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.exception.UnauthorizedException;
import com.hostel.management.repository.LeaveRequestRepository;
import com.hostel.management.repository.RoomAllocationRepository;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StudentRepository studentRepository;
    private final RoomAllocationRepository roomAllocationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LeaveResponse applyLeave(CreateLeaveRequest request, String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        // Business Rule 1: No past dates
        if (request.getFromDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("From date cannot be in the past");
        }

        // Business Rule 2: fromDate <= toDate
        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new BadRequestException("From date (" + request.getFromDate() +
                    ") cannot be after To date (" + request.getToDate() + ")");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .student(student)
                .leaveType(request.getLeaveType())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .reason(request.getReason().trim())
                .emergencyContactPhone(request.getEmergencyContactPhone().trim())
                .destinationAddress(request.getDestinationAddress() != null ? request.getDestinationAddress().trim() : null)
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public LeaveResponse approveLeave(Long id, LeaveActionRequest request) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only PENDING leave requests can be approved. Current status: " + leave.getStatus());
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedOrRejectedAt(LocalDateTime.now());
        if (request != null && request.getRemarks() != null) {
            leave.setAdminRemarks(request.getRemarks().trim());
        }

        LeaveRequest updated = leaveRequestRepository.save(leave);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public LeaveResponse rejectLeave(Long id, LeaveActionRequest request) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only PENDING leave requests can be rejected. Current status: " + leave.getStatus());
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedOrRejectedAt(LocalDateTime.now());
        if (request != null && request.getRemarks() != null) {
            leave.setAdminRemarks(request.getRemarks().trim());
        }

        LeaveRequest updated = leaveRequestRepository.save(leave);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public LeaveResponse cancelLeave(Long id, String currentUserEmail) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        // Ownership enforcement
        if (currentUser.getRole() == Role.STUDENT &&
                !leave.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to cancel this leave request");
        }

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only PENDING leave requests can be cancelled");
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setAdminRemarks("Cancelled by resident student");

        LeaveRequest updated = leaveRequestRepository.save(leave);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getLeaveById(Long id, String currentUserEmail) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        if (currentUser.getRole() == Role.STUDENT &&
                !leave.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this leave request");
        }

        return mapToResponse(leave);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> getAllLeaves(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            LeaveStatus status,
            LeaveType leaveType
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<LeaveRequest> leavePage = leaveRequestRepository.findLeavesWithFilters(
                search != null ? search.trim() : "",
                status,
                leaveType,
                pageable
        );

        List<LeaveResponse> responses = leavePage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<LeaveResponse>builder()
                .content(responses)
                .pageNumber(leavePage.getNumber())
                .pageSize(leavePage.getSize())
                .totalElements(leavePage.getTotalElements())
                .totalPages(leavePage.getTotalPages())
                .last(leavePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getMyLeaves(String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        return leaveRequestRepository.findByStudentIdOrderByCreatedAtDesc(student.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteLeave(Long id) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        leaveRequestRepository.delete(leave);
    }

    private LeaveResponse mapToResponse(LeaveRequest leave) {
        Student student = leave.getStudent();
        User user = student.getUser();

        Optional<RoomAllocation> activeAlloc = roomAllocationRepository
                .findByStudentIdAndStatus(student.getId(), AllocationStatus.ACTIVE);
        String roomNumber = activeAlloc.map(a -> a.getRoom().getRoomNumber()).orElse("N/A");

        long days = ChronoUnit.DAYS.between(leave.getFromDate(), leave.getToDate()) + 1;

        return LeaveResponse.builder()
                .id(leave.getId())
                .studentId(student.getId())
                .studentName(user.getName())
                .studentAdmissionNumber(student.getAdmissionNumber())
                .studentEmail(user.getEmail())
                .studentPhone(user.getPhone())
                .roomNumber(roomNumber)
                .leaveType(leave.getLeaveType())
                .fromDate(leave.getFromDate())
                .toDate(leave.getToDate())
                .numberOfDays(Math.max(1, days))
                .reason(leave.getReason())
                .emergencyContactPhone(leave.getEmergencyContactPhone())
                .destinationAddress(leave.getDestinationAddress())
                .status(leave.getStatus())
                .adminRemarks(leave.getAdminRemarks())
                .approvedOrRejectedAt(leave.getApprovedOrRejectedAt())
                .createdAt(leave.getCreatedAt())
                .updatedAt(leave.getUpdatedAt())
                .build();
    }
}
