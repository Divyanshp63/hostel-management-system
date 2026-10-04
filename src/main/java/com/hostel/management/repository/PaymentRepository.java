package com.hostel.management.repository;

import com.hostel.management.entity.Payment;
import com.hostel.management.enums.PaymentMethod;
import com.hostel.management.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);

    List<Payment> findByFeeIdOrderByPaymentDateDesc(Long feeId);

    List<Payment> findByStudentIdOrderByPaymentDateDesc(Long studentId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'SUCCESS'")
    BigDecimal sumSuccessfulPayments();

    Optional<Payment> findFirstByStudentIdAndPaymentStatusOrderByPaymentDateDesc(
            Long studentId, PaymentStatus paymentStatus
    );

    List<Payment> findByPaymentStatusOrderByPaymentDateDesc(PaymentStatus paymentStatus);

    @Query("SELECT p FROM Payment p JOIN p.student s JOIN s.user u JOIN p.fee f WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(p.transactionId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.admissionNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:method IS NULL OR p.paymentMethod = :method) AND " +
            "(:status IS NULL OR p.paymentStatus = :status)")
    Page<Payment> findPaymentsWithFilters(
            @Param("search") String search,
            @Param("method") PaymentMethod method,
            @Param("status") PaymentStatus status,
            Pageable pageable
    );
}
