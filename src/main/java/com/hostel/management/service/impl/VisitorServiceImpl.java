package com.hostel.management.service.impl;

import com.hostel.management.dto.request.CreateVisitorRequest;
import com.hostel.management.dto.request.VisitorApprovalRequest;
import com.hostel.management.dto.request.VisitorRejectionRequest;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.VisitorResponse;
import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.entity.VisitorRequest;
import com.hostel.management.enums.Role;
import com.hostel.management.enums.VisitorStatus;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.exception.UnauthorizedException;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.repository.VisitorRequestRepository;
import com.hostel.management.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitorServiceImpl implements VisitorService {

    private final VisitorRequestRepository visitorRequestRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

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
                .purpose(request.getPurpose().trim())
                .idProofType(request.getIdProofType() != null ? request.getIdProofType().trim() : null)
                .idProofNumber(request.getIdProofNumber() != null ? request.getIdProofNumber().trim() : null)
                .status(VisitorStatus.PENDING)
                .build();

        VisitorRequest saved = visitorRequestRepository.save(visitorRequest);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public VisitorResponse approveVisitorRequest(Long id, VisitorApprovalRequest request) {
        VisitorRequest visitorRequest = visitorRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor request not found with ID: " + id));

        if (visitorRequest.getStatus() != VisitorStatus.PENDING) {
            throw new BadRequestException("Only PENDING visitor requests can be approved. Current status: " + visitorRequest.getStatus());
        }

        visitorRequest.setStatus(VisitorStatus.APPROVED);
        visitorRequest.setCheckInTime(
                request.getCheckInTime() != null ? request.getCheckInTime() : LocalDateTime.now()
        );
        if (request.getAdminRemarks() != null) {
            visitorRequest.setAdminRemarks(request.getAdminRemarks().trim());
        }

        VisitorRequest updated = visitorRequestRepository.save(visitorRequest);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public VisitorResponse rejectVisitorRequest(Long id, VisitorRejectionRequest request) {
        VisitorRequest visitorRequest = visitorRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor request not found with ID: " + id));

        if (visitorRequest.getStatus() != VisitorStatus.PENDING) {
            throw new BadRequestException("Only PENDING visitor requests can be rejected. Current status: " + visitorRequest.getStatus());
        }

        visitorRequest.setStatus(VisitorStatus.REJECTED);
        visitorRequest.setAdminRemarks(request.getReason().trim());

        VisitorRequest updated = visitorRequestRepository.save(visitorRequest);
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

        // Ownership enforcement
        if (currentUser.getRole() == Role.STUDENT &&
                !visitorRequest.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to cancel this visitor request");
        }

        if (visitorRequest.getStatus() != VisitorStatus.PENDING) {
            throw new BadRequestException("Only PENDING visitor requests can be cancelled");
        }

        visitorRequest.setStatus(VisitorStatus.REJECTED);
        visitorRequest.setAdminRemarks("Cancelled by resident student");

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

        Page<VisitorRequest> requestPage = visitorRequestRepository.findVisitorsWithFilters(
                search != null ? search.trim() : "",
                status,
                visitDate,
                pageable
        );

        List<VisitorResponse> responses = requestPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<VisitorResponse>builder()
                .content(responses)
                .pageNumber(requestPage.getNumber())
                .pageSize(requestPage.getSize())
                .totalElements(requestPage.getTotalElements())
                .totalPages(requestPage.getTotalPages())
                .last(requestPage.isLast())
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

    private VisitorResponse mapToResponse(VisitorRequest request) {
        Student student = request.getStudent();
        User user = student.getUser();

        return VisitorResponse.builder()
                .id(request.getId())
                .studentId(student.getId())
                .studentName(user.getName())
                .studentAdmissionNumber(student.getAdmissionNumber())
                .studentPhone(user.getPhone())
                .visitorName(request.getVisitorName())
                .visitorPhone(request.getVisitorPhone())
                .relation(request.getRelation())
                .visitDate(request.getVisitDate())
                .checkInTime(request.getCheckInTime())
                .checkOutTime(request.getCheckOutTime())
                .status(request.getStatus())
                .idProofType(request.getIdProofType())
                .idProofNumber(request.getIdProofNumber())
                .purpose(request.getPurpose())
                .adminRemarks(request.getAdminRemarks())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
