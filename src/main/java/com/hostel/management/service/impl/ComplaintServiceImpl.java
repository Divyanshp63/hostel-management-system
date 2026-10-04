package com.hostel.management.service.impl;

import com.hostel.management.dto.request.CreateComplaintRequest;
import com.hostel.management.dto.request.UpdateComplaintStatusRequest;
import com.hostel.management.dto.response.ComplaintResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.entity.Complaint;
import com.hostel.management.entity.Room;
import com.hostel.management.entity.RoomAllocation;
import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.enums.AllocationStatus;
import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.ComplaintStatus;
import com.hostel.management.enums.Role;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.exception.UnauthorizedException;
import com.hostel.management.repository.ComplaintRepository;
import com.hostel.management.repository.RoomAllocationRepository;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final StudentRepository studentRepository;
    private final RoomAllocationRepository roomAllocationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ComplaintResponse createComplaint(CreateComplaintRequest request, String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        // Automatically associate with the student's active room (if assigned)
        Optional<RoomAllocation> activeAlloc = roomAllocationRepository
                .findByStudentIdAndStatus(student.getId(), AllocationStatus.ACTIVE);
        Room room = activeAlloc.map(RoomAllocation::getRoom).orElse(null);

        Complaint complaint = Complaint.builder()
                .student(student)
                .room(room)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .category(request.getCategory())
                .status(ComplaintStatus.PENDING)
                .build();

        Complaint saved = complaintRepository.save(complaint);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaintStatus(Long id, UpdateComplaintStatusRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        ComplaintStatus currentStatus = complaint.getStatus();
        ComplaintStatus newStatus = request.getStatus();

        // Business Rule: PENDING -> IN_PROGRESS -> RESOLVED only (no backward jump)
        if (currentStatus == ComplaintStatus.RESOLVED) {
            throw new BadRequestException("Complaint is already RESOLVED and cannot be modified further");
        }

        if (currentStatus == ComplaintStatus.IN_PROGRESS && newStatus == ComplaintStatus.PENDING) {
            throw new BadRequestException("Invalid status transition: Backward jump from IN_PROGRESS to PENDING is not allowed");
        }

        if (currentStatus == newStatus) {
            throw new BadRequestException("Complaint is already in status: " + newStatus);
        }

        if (newStatus == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        if (request.getResolutionRemarks() != null) {
            complaint.setResolutionRemarks(request.getResolutionRemarks().trim());
        }

        complaint.setStatus(newStatus);
        Complaint updated = complaintRepository.save(complaint);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id, String currentUserEmail) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        // Ownership enforcement: Students can only view their own complaint
        if (currentUser.getRole() == Role.STUDENT &&
                !complaint.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this complaint");
        }

        return mapToResponse(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ComplaintResponse> getAllComplaints(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            ComplaintStatus status,
            ComplaintCategory category
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Complaint> complaintPage = complaintRepository.findComplaintsWithFilters(
                search != null ? search.trim() : "",
                status,
                category,
                pageable
        );

        List<ComplaintResponse> responses = complaintPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<ComplaintResponse>builder()
                .content(responses)
                .pageNumber(complaintPage.getNumber())
                .pageSize(complaintPage.getSize())
                .totalElements(complaintPage.getTotalElements())
                .totalPages(complaintPage.getTotalPages())
                .last(complaintPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getMyComplaints(String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        return complaintRepository.findByStudentIdOrderByCreatedAtDesc(student.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteComplaint(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        complaintRepository.delete(complaint);
    }

    private ComplaintResponse mapToResponse(Complaint complaint) {
        Student student = complaint.getStudent();
        User user = student.getUser();
        String roomNumber = complaint.getRoom() != null ? complaint.getRoom().getRoomNumber() : "N/A";

        return ComplaintResponse.builder()
                .id(complaint.getId())
                .studentId(student.getId())
                .studentName(user.getName())
                .studentAdmissionNumber(student.getAdmissionNumber())
                .studentEmail(user.getEmail())
                .studentPhone(user.getPhone())
                .roomNumber(roomNumber)
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .category(complaint.getCategory())
                .status(complaint.getStatus())
                .resolutionRemarks(complaint.getResolutionRemarks())
                .resolvedAt(complaint.getResolvedAt())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .build();
    }
}
