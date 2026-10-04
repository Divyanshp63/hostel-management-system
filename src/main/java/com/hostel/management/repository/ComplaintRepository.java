package com.hostel.management.repository;

import com.hostel.management.entity.Complaint;
import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Complaint> findByStatus(ComplaintStatus status);

    long countByStatus(ComplaintStatus status);

    long countByStudentIdAndStatus(Long studentId, ComplaintStatus status);

    long countByStudentId(Long studentId);

    Optional<Complaint> findFirstByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Complaint> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category")
    List<Object[]> countComplaintsByCategory();

    @Query("SELECT c FROM Complaint c JOIN c.student s JOIN s.user u WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.admissionNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:category IS NULL OR c.category = :category)")
    Page<Complaint> findComplaintsWithFilters(
            @Param("search") String search,
            @Param("status") ComplaintStatus status,
            @Param("category") ComplaintCategory category,
            Pageable pageable
    );
}
