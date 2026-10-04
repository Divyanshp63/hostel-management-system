package com.hostel.management.service.impl;

import com.hostel.management.dto.request.BulkFeeGenerationRequest;
import com.hostel.management.dto.request.CreateFeeRequest;
import com.hostel.management.dto.request.UpdateFeeRequest;
import com.hostel.management.dto.response.FeeResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.entity.Fee;
import com.hostel.management.entity.RoomAllocation;
import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.enums.AllocationStatus;
import com.hostel.management.enums.FeeStatus;
import com.hostel.management.enums.Role;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.DuplicateResourceException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.exception.UnauthorizedException;
import com.hostel.management.repository.FeeRepository;
import com.hostel.management.repository.RoomAllocationRepository;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeServiceImpl implements FeeService {

    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;
    private final RoomAllocationRepository roomAllocationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FeeResponse createFee(CreateFeeRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        String normalizedMonth = request.getMonth().trim().toUpperCase();

        if (feeRepository.existsByStudentIdAndMonth(student.getId(), normalizedMonth)) {
            throw new DuplicateResourceException("Fee bill already exists for student " +
                    student.getAdmissionNumber() + " for month: " + normalizedMonth);
        }

        Fee fee = Fee.builder()
                .student(student)
                .month(normalizedMonth)
                .roomRent(request.getRoomRent())
                .messFee(request.getMessFee())
                .electricityFee(request.getElectricityFee())
                .maintenanceFee(request.getMaintenanceFee())
                .dueDate(request.getDueDate())
                .remarks(request.getRemarks() != null ? request.getRemarks().trim() : null)
                .paidAmount(BigDecimal.ZERO)
                .build();

        fee.recalculateTotalsAndStatus();
        Fee savedFee = feeRepository.save(fee);
        return mapToResponse(savedFee);
    }

    @Override
    @Transactional
    public List<FeeResponse> generateBulkFees(BulkFeeGenerationRequest request) {
        String normalizedMonth = request.getMonth().trim().toUpperCase();

        // Find all active room allocations to generate fees for current residents
        List<RoomAllocation> activeAllocations = roomAllocationRepository
                .findAll()
                .stream()
                .filter(a -> a.getStatus() == AllocationStatus.ACTIVE)
                .toList();

        List<FeeResponse> generatedFees = new ArrayList<>();

        for (RoomAllocation allocation : activeAllocations) {
            Student student = allocation.getStudent();

            // Skip if bill already generated for this student & month
            if (!feeRepository.existsByStudentIdAndMonth(student.getId(), normalizedMonth)) {
                Fee fee = Fee.builder()
                        .student(student)
                        .month(normalizedMonth)
                        .roomRent(allocation.getRoom().getRentPerMonth())
                        .messFee(request.getMessFee())
                        .electricityFee(request.getElectricityFee())
                        .maintenanceFee(request.getMaintenanceFee())
                        .dueDate(request.getDueDate())
                        .paidAmount(BigDecimal.ZERO)
                        .remarks("Automated bulk monthly fee invoice")
                        .build();

                fee.recalculateTotalsAndStatus();
                Fee savedFee = feeRepository.save(fee);
                generatedFees.add(mapToResponse(savedFee));
            }
        }

        return generatedFees;
    }

    @Override
    @Transactional
    public FeeResponse updateFee(Long id, UpdateFeeRequest request) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee invoice not found with ID: " + id));

        if (fee.getStatus() == FeeStatus.PAID) {
            throw new BadRequestException("Cannot edit fee invoice that has already been fully PAID");
        }

        fee.setRoomRent(request.getRoomRent());
        fee.setMessFee(request.getMessFee());
        fee.setElectricityFee(request.getElectricityFee());
        fee.setMaintenanceFee(request.getMaintenanceFee());
        fee.setDueDate(request.getDueDate());
        if (request.getRemarks() != null) {
            fee.setRemarks(request.getRemarks().trim());
        }

        fee.recalculateTotalsAndStatus();

        // Business rule: Total amount cannot be reduced below what has already been paid
        if (fee.getTotalAmount().compareTo(fee.getPaidAmount()) < 0) {
            throw new BadRequestException("Updated total amount (" + fee.getTotalAmount() +
                    ") cannot be less than already paid amount (" + fee.getPaidAmount() + ")");
        }

        Fee updatedFee = feeRepository.save(fee);
        return mapToResponse(updatedFee);
    }

    @Override
    @Transactional(readOnly = true)
    public FeeResponse getFeeById(Long id, String currentUserEmail) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee invoice not found with ID: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        // Ownership enforcement: Student can only view their own fee
        if (currentUser.getRole() == Role.STUDENT &&
                !fee.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this fee invoice");
        }

        return mapToResponse(fee);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FeeResponse> getAllFees(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            FeeStatus status,
            String month
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Fee> feePage = feeRepository.findFeesWithFilters(
                search != null ? search.trim() : "",
                status,
                month != null ? month.trim().toUpperCase() : "",
                pageable
        );

        List<FeeResponse> responses = feePage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<FeeResponse>builder()
                .content(responses)
                .pageNumber(feePage.getNumber())
                .pageSize(feePage.getSize())
                .totalElements(feePage.getTotalElements())
                .totalPages(feePage.getTotalPages())
                .last(feePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeeResponse> getMyFees(String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        return feeRepository.findByStudentIdOrderByDueDateDesc(student.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeeResponse> getFeesByStudentId(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentId);
        }

        return feeRepository.findByStudentIdOrderByDueDateDesc(studentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteFee(Long id) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee invoice not found with ID: " + id));

        if (fee.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Cannot delete fee invoice that already has recorded payments of ₹" + fee.getPaidAmount());
        }

        feeRepository.delete(fee);
    }

    private FeeResponse mapToResponse(Fee fee) {
        Student student = fee.getStudent();
        User user = student.getUser();

        return FeeResponse.builder()
                .id(fee.getId())
                .studentId(student.getId())
                .studentName(user.getName())
                .studentAdmissionNumber(student.getAdmissionNumber())
                .studentEmail(user.getEmail())
                .month(fee.getMonth())
                .roomRent(fee.getRoomRent())
                .messFee(fee.getMessFee())
                .electricityFee(fee.getElectricityFee())
                .maintenanceFee(fee.getMaintenanceFee())
                .totalAmount(fee.getTotalAmount())
                .paidAmount(fee.getPaidAmount())
                .pendingAmount(fee.getPendingAmount())
                .dueDate(fee.getDueDate())
                .status(fee.getStatus())
                .remarks(fee.getRemarks())
                .createdAt(fee.getCreatedAt())
                .updatedAt(fee.getUpdatedAt())
                .build();
    }
}
