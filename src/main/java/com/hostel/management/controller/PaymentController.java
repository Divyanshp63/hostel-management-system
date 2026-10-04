package com.hostel.management.controller;

import com.hostel.management.dto.request.PaymentRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.PaymentResponse;
import com.hostel.management.enums.PaymentMethod;
import com.hostel.management.enums.PaymentStatus;
import com.hostel.management.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PaymentResponse response = paymentService.processPayment(request, userDetails.getUsername());
        return new ResponseEntity<>(
                ApiResponse.success("Payment processed successfully and balance updated", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) PaymentStatus status
    ) {
        PageResponse<PaymentResponse> response = paymentService.getAllPayments(
                page, size, sortBy, sortDir, search, method, status
        );
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", response));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<PaymentResponse> response = paymentService.getMyPayments(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Your payment history retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PaymentResponse response = paymentService.getPaymentById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Payment details retrieved successfully", response));
    }

    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByTransactionId(
            @PathVariable String transactionId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        PaymentResponse response = paymentService.getPaymentByTransactionId(transactionId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Payment receipt retrieved successfully", response));
    }

    @GetMapping("/fee/{feeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByFeeId(
            @PathVariable Long feeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<PaymentResponse> response = paymentService.getPaymentsByFeeId(feeId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Fee payments retrieved successfully", response));
    }
}
