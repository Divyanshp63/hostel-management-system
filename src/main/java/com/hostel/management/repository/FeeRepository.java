package com.hostel.management.repository;

import com.hostel.management.entity.Fee;
import com.hostel.management.enums.FeeStatus;
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
public interface FeeRepository extends JpaRepository<Fee, Long> {

    Optional<Fee> findByStudentIdAndMonth(Long studentId, String month);

    boolean existsByStudentIdAndMonth(Long studentId, String month);

    List<Fee> findByStudentIdOrderByDueDateDesc(Long studentId);

    List<Fee> findByStatus(FeeStatus status);

    long countByStatus(FeeStatus status);

    @Query("SELECT COALESCE(SUM(f.totalAmount), 0) FROM Fee f")
    BigDecimal sumTotalBilled();

    @Query("SELECT COALESCE(SUM(f.paidAmount), 0) FROM Fee f")
    BigDecimal sumTotalPaid();

    @Query("SELECT COALESCE(SUM(f.remainingAmount), 0) FROM Fee f")
    BigDecimal sumTotalRemaining();

    @Query("SELECT COALESCE(SUM(f.remainingAmount), 0) FROM Fee f WHERE f.student.id = :studentId")
    BigDecimal sumRemainingByStudentId(@Param("studentId") Long studentId);

    Optional<Fee> findFirstByStudentIdOrderByCreatedAtDesc(Long studentId);

    @Query("SELECT f FROM Fee f JOIN f.student s JOIN s.user u WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.admissionNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR f.status = :status) AND " +
            "(:month IS NULL OR :month = '' OR f.month = :month)")
    Page<Fee> findFeesWithFilters(
            @Param("search") String search,
            @Param("status") FeeStatus status,
            @Param("month") String month,
            Pageable pageable
    );
}
