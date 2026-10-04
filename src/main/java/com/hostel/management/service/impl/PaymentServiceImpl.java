package com.hostel.management.service.impl;

import com.hostel.management.dto.request.PaymentRequest;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.PaymentResponse;
import com.hostel.management.entity.Fee;
import com.hostel.management.entity.Payment;
import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.enums.FeeStatus;
import com.hostel.management.enums.PaymentMethod;
import com.hostel.management.enums.PaymentStatus;
import com.hostel.management.enums.Role;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.exception.UnauthorizedException;
import com.hostel.management.repository.FeeRepository;
import com.hostel.management.repository.PaymentRepository;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final FeeRepository feeRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, String currentUserEmail) {
        Fee fee = feeRepository.findById(request.getFeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Fee invoice not found with ID: " + request.getFeeId()));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        // Ownership enforcement: If caller is STUDENT, they can only pay their own fee
        if (currentUser.getRole() == Role.STUDENT &&
                !fee.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to make a payment for another student's fee invoice");
        }

        // Business Rule: Check if fee is already fully paid
        if (fee.getStatus() == FeeStatus.PAID || fee.getPendingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Fee invoice for month " + fee.getMonth() + " is already fully PAID");
        }

        // Business Rule: Block overpayment
        if (request.getAmount().compareTo(fee.getPendingAmount()) > 0) {
            throw new BadRequestException("Payment amount (₹" + request.getAmount() +
                    ") cannot exceed current pending fee balance (₹" + fee.getPendingAmount() + ")");
        }

        // Generate unique transaction reference
        String transactionId = "TXN-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Update fee balance and recalculate status automatically
        fee.setPaidAmount(fee.getPaidAmount().add(request.getAmount()));
        fee.recalculateTotalsAndStatus();
        Fee updatedFee = feeRepository.save(fee);

        Payment payment = Payment.builder()
                .fee(updatedFee)
                .student(updatedFee.getStudent())
                .transactionId(transactionId)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
                .remarks(request.getRemarks() != null ? request.getRemarks().trim() : "Payment processed successfully")
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id, String currentUserEmail) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found with ID: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        if (currentUser.getRole() == Role.STUDENT &&
                !payment.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this payment receipt");
        }

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByTransactionId(String transactionId, String currentUserEmail) {
        Payment payment = paymentRepository.findByTransactionId(transactionId.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for transaction: " + transactionId));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        if (currentUser.getRole() == Role.STUDENT &&
                !payment.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this payment receipt");
        }

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getAllPayments(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            PaymentMethod method,
            PaymentStatus status
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Payment> paymentPage = paymentRepository.findPaymentsWithFilters(
                search != null ? search.trim() : "",
                method,
                status,
                pageable
        );

        List<PaymentResponse> responses = paymentPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<PaymentResponse>builder()
                .content(responses)
                .pageNumber(paymentPage.getNumber())
                .pageSize(paymentPage.getSize())
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .last(paymentPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        return paymentRepository.findByStudentIdOrderByPaymentDateDesc(student.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByFeeId(Long feeId, String currentUserEmail) {
        Fee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new ResourceNotFoundException("Fee invoice not found with ID: " + feeId));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        if (currentUser.getRole() == Role.STUDENT &&
                !fee.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view payments for this invoice");
        }

        return paymentRepository.findByFeeIdOrderByPaymentDateDesc(feeId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        Fee fee = payment.getFee();
        Student student = payment.getStudent();
        User user = student.getUser();

        return PaymentResponse.builder()
                .id(payment.getId())
                .transactionId(payment.getTransactionId())
                .feeId(fee.getId())
                .feeMonth(fee.getMonth())
                .feeTotalAmount(fee.getTotalAmount())
                .feeRemainingPending(fee.getPendingAmount())
                .updatedFeeStatus(fee.getStatus())
                .studentId(student.getId())
                .studentName(user.getName())
                .studentAdmissionNumber(student.getAdmissionNumber())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .remarks(payment.getRemarks())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
