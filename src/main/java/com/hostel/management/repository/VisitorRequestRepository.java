package com.hostel.management.repository;

import com.hostel.management.entity.VisitorRequest;
import com.hostel.management.enums.VisitorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VisitorRequestRepository extends JpaRepository<VisitorRequest, Long> {

    List<VisitorRequest> findByStudentIdOrderByVisitDateDesc(Long studentId);

    List<VisitorRequest> findByStatus(VisitorStatus status);

    long countByStatus(VisitorStatus status);

    long countByStudentIdAndStatus(Long studentId, VisitorStatus status);

    long countByVisitDateAndStatus(LocalDate visitDate, VisitorStatus status);

    long countByVisitDate(LocalDate visitDate);

    @Query("SELECT v FROM VisitorRequest v JOIN v.student s JOIN s.user u WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(v.visitorName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.admissionNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR v.status = :status) AND " +
            "(:visitDate IS NULL OR v.visitDate = :visitDate)")
    Page<VisitorRequest> findVisitorsWithFilters(
            @Param("search") String search,
            @Param("status") VisitorStatus status,
            @Param("visitDate") LocalDate visitDate,
            Pageable pageable
    );
}
