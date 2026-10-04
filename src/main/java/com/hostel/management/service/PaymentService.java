package com.hostel.management.service;

import com.hostel.management.dto.request.PaymentRequest;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.PaymentResponse;
import com.hostel.management.enums.PaymentMethod;
import com.hostel.management.enums.PaymentStatus;

import java.util.List;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request, String currentUserEmail);

    PaymentResponse getPaymentById(Long id, String currentUserEmail);

    PaymentResponse getPaymentByTransactionId(String transactionId, String currentUserEmail);

    PageResponse<PaymentResponse> getAllPayments(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            PaymentMethod method,
            PaymentStatus status
    );

    List<PaymentResponse> getMyPayments(String currentUserEmail);

    List<PaymentResponse> getPaymentsByFeeId(Long feeId, String currentUserEmail);
}
