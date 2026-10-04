package com.hostel.management.repository;

import com.hostel.management.entity.LeaveRequest;
import com.hostel.management.enums.LeaveStatus;
import com.hostel.management.enums.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<LeaveRequest> findByStatus(LeaveStatus status);

    long countByStatus(LeaveStatus status);

    long countByStudentIdAndStatus(Long studentId, LeaveStatus status);

    @Query("SELECT l FROM LeaveRequest l JOIN l.student s JOIN s.user u WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.admissionNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR l.status = :status) AND " +
            "(:leaveType IS NULL OR l.leaveType = :leaveType)")
    Page<LeaveRequest> findLeavesWithFilters(
            @Param("search") String search,
            @Param("status") LeaveStatus status,
            @Param("leaveType") LeaveType leaveType,
            Pageable pageable
    );
}
