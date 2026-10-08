package com.hostel.management.service.impl;

import com.hostel.management.dto.request.CreateVisitorRequest;
import com.hostel.management.dto.request.VisitorApprovalRequest;
import com.hostel.management.dto.request.VisitorRejectionRequest;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.VisitorResponse;
import com.hostel.management.entity.RoomAllocation;
import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.entity.VisitorRequest;
import com.hostel.management.enums.AllocationStatus;
import com.hostel.management.enums.Role;
import com.hostel.management.enums.VisitorStatus;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.exception.UnauthorizedException;
import com.hostel.management.repository.RoomAllocationRepository;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.repository.VisitorRequestRepository;
import com.hostel.management.service.AuditLogService;
import com.hostel.management.service.VisitorService;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitorServiceImpl implements VisitorService {

    private final VisitorRequestRepository visitorRequestRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final RoomAllocationRepository roomAllocationRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public VisitorResponse requestVisitorPass(CreateVisitorRequest request, String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        if (request.getVisitDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Visit date cannot be in the past");
        }

        VisitorRequest visitorRequest = VisitorRequest.builder()
                .student(student)
                .visitorName(request.getVisitorName().trim())
                .visitorPhone(request.getVisitorPhone().trim())
                .relation(request.getRelation().trim())
                .visitDate(request.getVisitDate())
                .visitTime(request.getVisitTime() != null ? request.getVisitTime().trim() : null)
                .purpose(request.getPurpose().trim())
                .idProofType(request.getIdProofType() != null ? request.getIdProofType().trim() : null)
                .idProofNumber(request.getIdProofNumber() != null ? request.getIdProofNumber().trim() : null)
                .status(VisitorStatus.PENDING)
                .build();

        VisitorRequest saved = visitorRequestRepository.save(visitorRequest);
        log.info("Visitor request #{} submitted by student {}", saved.getId(), student.getUser().getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public VisitorResponse approveVisitorRequest(Long id, VisitorApprovalRequest request) {
        return approveVisitorRequest(id, request, null);
    }

    @Override
    @Transactional
    public VisitorResponse approveVisitorRequest(Long id, VisitorApprovalRequest request, String wardenEmail) {
        VisitorRequest visitorRequest = visitorRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor request not found with ID: " + id));

        if (visitorRequest.getStatus() != VisitorStatus.PENDING) {
            throw new BadRequestException("Only PENDING visitor requests can be approved. Current status: " + visitorRequest.getStatus());
        }

        User warden = null;
        if (wardenEmail != null) {
            warden = userRepository.findByEmail(wardenEmail.trim().toLowerCase()).orElse(null);
        }

        visitorRequest.setStatus(VisitorStatus.APPROVED);
        visitorRequest.setApprovedBy(warden);
        visitorRequest.setApprovedOrRejectedAt(LocalDateTime.now());
        visitorRequest.setCheckInTime(
                request != null && request.getCheckInTime() != null ? request.getCheckInTime() : LocalDateTime.now()
        );
        if (request != null && request.getAdminRemarks() != null && !request.getAdminRemarks().isBlank()) {
            visitorRequest.setAdminRemarks(request.getAdminRemarks().trim());
        } else {
            visitorRequest.setAdminRemarks("Approved by Warden");
        }

        VisitorRequest updated = visitorRequestRepository.save(visitorRequest);

        auditLogService.log(
                warden != null ? warden.getId() : null,
                warden != null ? warden.getName() : "Warden",
                "WARDEN",
                "VISITOR_APPROVED",
                "VISITOR",
                String.valueOf(updated.getId()),
                "Visitor pass approved for " + updated.getVisitorName() + " (Visiting: " + updated.getStudent().getUser().getName() + ")"
        );

        log.info("Visitor request #{} APPROVED by {}", id, warden != null ? warden.getEmail() : "system");
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public VisitorResponse rejectVisitorRequest(Long id, VisitorRejectionRequest request) {
        return rejectVisitorRequest(id, request, null);
    }

    @Override
    @Transactional
    public VisitorResponse rejectVisitorRequest(Long id, VisitorRejectionRequest request, String wardenEmail) {
        VisitorRequest visitorRequest = visitorRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor request not found with ID: " + id));

        if (visitorRequest.getStatus() != VisitorStatus.PENDING) {
            throw new BadRequestException("Only PENDING visitor requests can be rejected. Current status: " + visitorRequest.getStatus());
        }

        User warden = null;
        if (wardenEmail != null) {
            warden = userRepository.findByEmail(wardenEmail.trim().toLowerCase()).orElse(null);
        }

        visitorRequest.setStatus(VisitorStatus.REJECTED);
        visitorRequest.setApprovedBy(warden);
        visitorRequest.setApprovedOrRejectedAt(LocalDateTime.now());
        if (request != null && request.getReason() != null && !request.getReason().isBlank()) {
            visitorRequest.setAdminRemarks(request.getReason().trim());
        } else {
            visitorRequest.setAdminRemarks("Rejected by Warden");
        }

        VisitorRequest updated = visitorRequestRepository.save(visitorRequest);

        auditLogService.log(
                warden != null ? warden.getId() : null,
                warden != null ? warden.getName() : "Warden",
                "WARDEN",
                "VISITOR_REJECTED",
                "VISITOR",
                String.valueOf(updated.getId()),
                "Visitor pass rejected for " + updated.getVisitorName() + ". Reason: " + updated.getAdminRemarks()
        );

        log.info("Visitor request #{} REJECTED by {}", id, warden != null ? warden.getEmail() : "system");
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public VisitorResponse completeVisit(Long id) {
        VisitorRequest visitorRequest = visitorRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor request not found with ID: " + id));

        if (visitorRequest.getStatus() != VisitorStatus.APPROVED) {
            throw new BadRequestException("Only APPROVED visits can be marked as COMPLETED. Current status: " + visitorRequest.getStatus());
        }

        visitorRequest.setStatus(VisitorStatus.COMPLETED);
        visitorRequest.setCheckOutTime(LocalDateTime.now());

        VisitorRequest updated = visitorRequestRepository.save(visitorRequest);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public VisitorResponse cancelVisitorRequest(Long id, String currentUserEmail) {
        VisitorRequest visitorRequest = visitorRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor request not found with ID: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        if (currentUser.getRole() == Role.STUDENT &&
                !visitorRequest.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to cancel this visitor request");
        }

        if (visitorRequest.getStatus() != VisitorStatus.PENDING) {
            throw new BadRequestException("Only PENDING visitor requests can be cancelled");
        }

        visitorRequest.setStatus(VisitorStatus.REJECTED);
        visitorRequest.setAdminRemarks("Cancelled by resident student");
        visitorRequest.setApprovedOrRejectedAt(LocalDateTime.now());

        VisitorRequest updated = visitorRequestRepository.save(visitorRequest);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public VisitorResponse getVisitorRequestById(Long id, String currentUserEmail) {
        VisitorRequest visitorRequest = visitorRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor request not found with ID: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        if (currentUser.getRole() == Role.STUDENT &&
                !visitorRequest.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this visitor pass");
        }

        return mapToResponse(visitorRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VisitorResponse> getAllVisitorRequests(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            VisitorStatus status,
            LocalDate visitDate
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<VisitorRequest> visitorPage = visitorRequestRepository.findVisitorsWithFilters(
                search != null ? search.trim() : "",
                status,
                visitDate,
                pageable
        );

        List<VisitorResponse> responses = visitorPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<VisitorResponse>builder()
                .content(responses)
                .pageNumber(visitorPage.getNumber())
                .pageSize(visitorPage.getSize())
                .totalElements(visitorPage.getTotalElements())
                .totalPages(visitorPage.getTotalPages())
                .last(visitorPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitorResponse> getMyVisitorRequests(String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        return visitorRequestRepository.findByStudentIdOrderByVisitDateDesc(student.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteVisitorRequest(Long id) {
        VisitorRequest visitorRequest = visitorRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor request not found with ID: " + id));

        visitorRequestRepository.delete(visitorRequest);
    }

    private VisitorResponse mapToResponse(VisitorRequest v) {
        Student student = v.getStudent();
        User user = student.getUser();

        Optional<RoomAllocation> activeAlloc = roomAllocationRepository
                .findByStudentIdAndStatus(student.getId(), AllocationStatus.ACTIVE);
        String roomNumber = activeAlloc.map(a -> a.getRoom().getRoomNumber()).orElse("N/A");

        return VisitorResponse.builder()
                .id(v.getId())
                .studentId(student.getId())
                .studentName(user.getName())
                .studentAdmissionNumber(student.getAdmissionNumber())
                .studentPhone(user.getPhone())
                .roomNumber(roomNumber)
                .visitorName(v.getVisitorName())
                .visitorPhone(v.getVisitorPhone())
                .relation(v.getRelation())
                .visitDate(v.getVisitDate())
                .visitTime(v.getVisitTime())
                .checkInTime(v.getCheckInTime())
                .checkOutTime(v.getCheckOutTime())
                .status(v.getStatus())
                .purpose(v.getPurpose())
                .idProofType(v.getIdProofType())
                .idProofNumber(v.getIdProofNumber())
                .adminRemarks(v.getAdminRemarks())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
