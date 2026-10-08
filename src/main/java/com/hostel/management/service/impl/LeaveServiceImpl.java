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
import com.hostel.management.service.AuditLogService;
import com.hostel.management.service.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StudentRepository studentRepository;
    private final RoomAllocationRepository roomAllocationRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

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

        // Business Rule 2.1: Non-empty reason
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BadRequestException("Leave reason cannot be empty");
        }

        // Business Rule 3: Check overlapping active/pending leaves
        long overlapping = leaveRequestRepository.countOverlappingActiveLeaves(student.getId(), request.getFromDate(), request.getToDate());
        if (overlapping > 0) {
            throw new BadRequestException("You already have an active or pending leave pass covering these dates. Please review existing requests.");
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
        log.info("Leave request created for student {} (ID: {})", student.getUser().getName(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public LeaveResponse approveLeave(Long id, LeaveActionRequest request) {
        return approveLeave(id, request, null);
    }

    @Override
    @Transactional
    public LeaveResponse approveLeave(Long id, LeaveActionRequest request, String wardenEmail) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only PENDING leave requests can be approved. Current status: " + leave.getStatus());
        }

        User warden = null;
        if (wardenEmail != null) {
            warden = userRepository.findByEmail(wardenEmail.trim().toLowerCase()).orElse(null);
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedOrRejectedAt(LocalDateTime.now());
        leave.setApprovedBy(warden);
        if (request != null && request.getRemarks() != null && !request.getRemarks().isBlank()) {
            leave.setAdminRemarks(request.getRemarks().trim());
        } else if (leave.getAdminRemarks() == null) {
            leave.setAdminRemarks("Approved by Warden");
        }

        LeaveRequest updated = leaveRequestRepository.save(leave);

        auditLogService.log(
                warden != null ? warden.getId() : null,
                warden != null ? warden.getName() : "Warden",
                "WARDEN",
                "LEAVE_APPROVED",
                "LEAVE",
                String.valueOf(leave.getId()),
                "Leave approved for " + leave.getStudent().getUser().getName() + " (" + leave.getFromDate() + " to " + leave.getToDate() + ")"
        );

        log.info("Leave request #{} APPROVED by {}", id, warden != null ? warden.getEmail() : "system");
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public LeaveResponse rejectLeave(Long id, LeaveActionRequest request) {
        return rejectLeave(id, request, null);
    }

    @Override
    @Transactional
    public LeaveResponse rejectLeave(Long id, LeaveActionRequest request, String wardenEmail) {
        LeaveRequest leave = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + id));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only PENDING leave requests can be rejected. Current status: " + leave.getStatus());
        }

        User warden = null;
        if (wardenEmail != null) {
            warden = userRepository.findByEmail(wardenEmail.trim().toLowerCase()).orElse(null);
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedOrRejectedAt(LocalDateTime.now());
        leave.setApprovedBy(warden);
        if (request != null && request.getRemarks() != null && !request.getRemarks().isBlank()) {
            leave.setAdminRemarks(request.getRemarks().trim());
        } else {
            leave.setAdminRemarks("Rejected by Warden");
        }

        LeaveRequest updated = leaveRequestRepository.save(leave);

        auditLogService.log(
                warden != null ? warden.getId() : null,
                warden != null ? warden.getName() : "Warden",
                "WARDEN",
                "LEAVE_REJECTED",
                "LEAVE",
                String.valueOf(leave.getId()),
                "Leave rejected for " + leave.getStudent().getUser().getName() + ". Reason: " + leave.getAdminRemarks()
        );

        log.info("Leave request #{} REJECTED by {}", id, warden != null ? warden.getEmail() : "system");
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

        leave.setStatus(LeaveStatus.CANCELLED);
        leave.setAdminRemarks("Cancelled by resident student");
        leave.setApprovedOrRejectedAt(LocalDateTime.now());

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
