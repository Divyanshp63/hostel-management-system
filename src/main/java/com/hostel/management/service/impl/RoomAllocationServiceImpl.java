package com.hostel.management.service.impl;

import com.hostel.management.dto.request.AdminAllocationRequest;
import com.hostel.management.dto.request.ApprovalRequest;
import com.hostel.management.dto.request.RejectionRequest;
import com.hostel.management.dto.request.RoomAllocationRequest;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.RoomAllocationResponse;
import com.hostel.management.entity.Room;
import com.hostel.management.entity.RoomAllocation;
import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.enums.AllocationStatus;
import com.hostel.management.enums.Role;
import com.hostel.management.enums.RoomStatus;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.exception.UnauthorizedException;
import com.hostel.management.repository.RoomAllocationRepository;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.RoomAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomAllocationServiceImpl implements RoomAllocationService {

    private final RoomAllocationRepository roomAllocationRepository;
    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RoomAllocationResponse requestAllocation(RoomAllocationRequest request, String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        // Business Rule 1: A student can have only one ACTIVE allocation
        if (roomAllocationRepository.existsByStudentIdAndStatus(student.getId(), AllocationStatus.ACTIVE)) {
            throw new BadRequestException("You already have an ACTIVE room allocation. Please vacate your current room first.");
        }

        // Business Rule 2: Cannot make multiple pending requests
        if (roomAllocationRepository.existsByStudentIdAndStatus(student.getId(), AllocationStatus.PENDING)) {
            throw new BadRequestException("You already have a PENDING room allocation request awaiting warden approval.");
        }

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.getRoomId()));

        if (room.getStatus() == RoomStatus.UNDER_MAINTENANCE) {
            throw new BadRequestException("Room " + room.getRoomNumber() + " is currently under maintenance");
        }

        if (room.getAvailableBeds() <= 0) {
            throw new BadRequestException("Room " + room.getRoomNumber() + " has no available beds");
        }

        RoomAllocation allocation = RoomAllocation.builder()
                .student(student)
                .room(room)
                .status(AllocationStatus.PENDING)
                .requestDate(LocalDate.now())
                .remarks(request.getRemarks() != null ? request.getRemarks().trim() : null)
                .build();

        RoomAllocation savedAllocation = roomAllocationRepository.save(allocation);
        return mapToResponse(savedAllocation);
    }

    @Override
    @Transactional
    public RoomAllocationResponse directAllocate(AdminAllocationRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        if (roomAllocationRepository.existsByStudentIdAndStatus(student.getId(), AllocationStatus.ACTIVE)) {
            throw new BadRequestException("Student already has an ACTIVE room allocation");
        }

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.getRoomId()));

        if (room.getStatus() == RoomStatus.UNDER_MAINTENANCE) {
            throw new BadRequestException("Room " + room.getRoomNumber() + " is currently under maintenance");
        }

        if (room.getAvailableBeds() <= 0) {
            throw new BadRequestException("Room " + room.getRoomNumber() + " is full");
        }

        // Increment occupied count and update status
        room.setOccupied(room.getOccupied() + 1);
        room.updateStatusBasedOnOccupancy();
        roomRepository.save(room);

        RoomAllocation allocation = RoomAllocation.builder()
                .student(student)
                .room(room)
                .bedNumber(request.getBedNumber().trim().toUpperCase())
                .status(AllocationStatus.ACTIVE)
                .requestDate(LocalDate.now())
                .startDate(LocalDate.now())
                .remarks(request.getRemarks() != null ? request.getRemarks().trim() : "Directly allocated by Warden")
                .build();

        RoomAllocation savedAllocation = roomAllocationRepository.save(allocation);
        return mapToResponse(savedAllocation);
    }

    @Override
    @Transactional
    public RoomAllocationResponse approveAllocation(Long allocationId, ApprovalRequest request) {
        RoomAllocation allocation = roomAllocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Room allocation not found with ID: " + allocationId));

        if (allocation.getStatus() != AllocationStatus.PENDING) {
            throw new BadRequestException("Only PENDING allocation requests can be approved. Current status: " + allocation.getStatus());
        }

        Student student = allocation.getStudent();
        if (roomAllocationRepository.existsByStudentIdAndStatus(student.getId(), AllocationStatus.ACTIVE)) {
            throw new BadRequestException("Student already has another ACTIVE room allocation");
        }

        Room room = allocation.getRoom();
        if (room.getStatus() == RoomStatus.UNDER_MAINTENANCE) {
            throw new BadRequestException("Room " + room.getRoomNumber() + " is currently under maintenance");
        }

        if (room.getAvailableBeds() <= 0) {
            throw new BadRequestException("Room " + room.getRoomNumber() + " has no available beds");
        }

        // Increase room occupancy
        room.setOccupied(room.getOccupied() + 1);
        room.updateStatusBasedOnOccupancy();
        roomRepository.save(room);

        allocation.setStatus(AllocationStatus.ACTIVE);
        allocation.setBedNumber(request.getBedNumber().trim().toUpperCase());
        allocation.setStartDate(LocalDate.now());
        if (request.getRemarks() != null) {
            allocation.setRemarks(request.getRemarks().trim());
        }

        RoomAllocation saved = roomAllocationRepository.save(allocation);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public RoomAllocationResponse rejectAllocation(Long allocationId, RejectionRequest request) {
        RoomAllocation allocation = roomAllocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Room allocation not found with ID: " + allocationId));

        if (allocation.getStatus() != AllocationStatus.PENDING) {
            throw new BadRequestException("Only PENDING allocation requests can be rejected. Current status: " + allocation.getStatus());
        }

        allocation.setStatus(AllocationStatus.REJECTED);
        allocation.setRejectionReason(request.getReason().trim());

        RoomAllocation saved = roomAllocationRepository.save(allocation);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public RoomAllocationResponse vacateAllocation(Long allocationId) {
        RoomAllocation allocation = roomAllocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Room allocation not found with ID: " + allocationId));

        if (allocation.getStatus() != AllocationStatus.ACTIVE) {
            throw new BadRequestException("Only ACTIVE allocations can be vacated. Current status: " + allocation.getStatus());
        }

        allocation.setStatus(AllocationStatus.VACATED);
        allocation.setEndDate(LocalDate.now());

        // Decrease room occupied count
        Room room = allocation.getRoom();
        room.setOccupied(Math.max(0, room.getOccupied() - 1));
        room.updateStatusBasedOnOccupancy();
        roomRepository.save(room);

        RoomAllocation saved = roomAllocationRepository.save(allocation);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public RoomAllocationResponse cancelAllocationRequest(Long allocationId, String currentUserEmail) {
        RoomAllocation allocation = roomAllocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Room allocation not found with ID: " + allocationId));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        // Ownership enforcement: Students can only cancel their own requests
        if (currentUser.getRole() == Role.STUDENT &&
                !allocation.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to cancel this allocation request");
        }

        if (allocation.getStatus() != AllocationStatus.PENDING) {
            throw new BadRequestException("Only PENDING requests can be cancelled. Current status: " + allocation.getStatus());
        }

        allocation.setStatus(AllocationStatus.CANCELLED);
        RoomAllocation saved = roomAllocationRepository.save(allocation);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoomAllocationResponse> getAllAllocations(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            AllocationStatus status
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<RoomAllocation> allocationPage = roomAllocationRepository.findAllocationsWithFilters(
                search != null ? search.trim() : "",
                status,
                pageable
        );

        List<RoomAllocationResponse> responses = allocationPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<RoomAllocationResponse>builder()
                .content(responses)
                .pageNumber(allocationPage.getNumber())
                .pageSize(allocationPage.getSize())
                .totalElements(allocationPage.getTotalElements())
                .totalPages(allocationPage.getTotalPages())
                .last(allocationPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomAllocationResponse> getMyAllocations(String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        return roomAllocationRepository.findByStudentIdOrderByCreatedAtDesc(student.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomAllocationResponse getAllocationById(Long id, String currentUserEmail) {
        RoomAllocation allocation = roomAllocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room allocation not found with ID: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        // Ownership enforcement: Student can only view their own allocation
        if (currentUser.getRole() == Role.STUDENT &&
                !allocation.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this allocation");
        }

        return mapToResponse(allocation);
    }

    private RoomAllocationResponse mapToResponse(RoomAllocation allocation) {
        Student student = allocation.getStudent();
        Room room = allocation.getRoom();

        return RoomAllocationResponse.builder()
                .id(allocation.getId())
                .studentId(student.getId())
                .studentName(student.getUser().getName())
                .studentAdmissionNumber(student.getAdmissionNumber())
                .studentEmail(student.getUser().getEmail())
                .studentPhone(student.getUser().getPhone())
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .floor(room.getFloor())
                .roomType(room.getRoomType())
                .rentPerMonth(room.getRentPerMonth())
                .bedNumber(allocation.getBedNumber())
                .status(allocation.getStatus())
                .requestDate(allocation.getRequestDate())
                .startDate(allocation.getStartDate())
                .endDate(allocation.getEndDate())
                .rejectionReason(allocation.getRejectionReason())
                .remarks(allocation.getRemarks())
                .createdAt(allocation.getCreatedAt())
                .updatedAt(allocation.getUpdatedAt())
                .build();
    }
}
