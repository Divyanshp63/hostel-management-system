package com.hostel.management.repository;

import com.hostel.management.entity.Complaint;
import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.ComplaintPriority;
import com.hostel.management.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Complaint> findByStatus(ComplaintStatus status);

    List<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status);

    long countByStatus(ComplaintStatus status);

    long countByStatusIn(Collection<ComplaintStatus> statuses);

    long countByStudentIdAndStatus(Long studentId, ComplaintStatus status);

    long countByStudentIdAndStatusIn(Long studentId, Collection<ComplaintStatus> statuses);

    long countByStudentId(Long studentId);

    Optional<Complaint> findFirstByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Complaint> findTop5ByOrderByCreatedAtDesc();

    // Staff / Maintenance Queries
    List<Complaint> findByAssignedStaffIdOrderByCreatedAtDesc(Long staffId);

    List<Complaint> findByAssignedDepartmentOrderByCreatedAtDesc(ComplaintCategory department);

    @Query("SELECT c FROM Complaint c WHERE (c.assignedStaff.id = :staffId OR c.assignedDepartment = :department OR c.assignedDepartment = com.hostel.management.enums.ComplaintCategory.MAINTENANCE) ORDER BY c.createdAt DESC")
    List<Complaint> findForStaffMember(@Param("staffId") Long staffId, @Param("department") ComplaintCategory department);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE (c.assignedStaff.id = :staffId OR c.assignedDepartment = :department OR c.assignedDepartment = com.hostel.management.enums.ComplaintCategory.MAINTENANCE)")
    long countForStaffMember(@Param("staffId") Long staffId, @Param("department") ComplaintCategory department);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE (c.assignedStaff.id = :staffId OR c.assignedDepartment = :department OR c.assignedDepartment = com.hostel.management.enums.ComplaintCategory.MAINTENANCE) AND c.status = :status")
    long countForStaffMemberByStatus(@Param("staffId") Long staffId, @Param("department") ComplaintCategory department, @Param("status") ComplaintStatus status);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE (c.assignedStaff.id = :staffId OR c.assignedDepartment = :department OR c.assignedDepartment = com.hostel.management.enums.ComplaintCategory.MAINTENANCE) AND c.priority = :priority")
    long countForStaffMemberByPriority(@Param("staffId") Long staffId, @Param("department") ComplaintCategory department, @Param("priority") ComplaintPriority priority);

    @Query("SELECT COUNT(c) FROM Complaint c WHERE (c.assignedStaff.id = :staffId OR c.assignedDepartment = :department OR c.assignedDepartment = com.hostel.management.enums.ComplaintCategory.MAINTENANCE) AND c.status NOT IN (com.hostel.management.enums.ComplaintStatus.RESOLVED_BY_MAINTENANCE, com.hostel.management.enums.ComplaintStatus.VERIFIED, com.hostel.management.enums.ComplaintStatus.CLOSED, com.hostel.management.enums.ComplaintStatus.REJECTED) AND c.slaDeadline < :now")
    long countSlaBreachedForStaff(@Param("staffId") Long staffId, @Param("department") ComplaintCategory department, @Param("now") LocalDateTime now);

    // Warden SLA Queries
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status NOT IN (com.hostel.management.enums.ComplaintStatus.RESOLVED_BY_MAINTENANCE, com.hostel.management.enums.ComplaintStatus.VERIFIED, com.hostel.management.enums.ComplaintStatus.CLOSED, com.hostel.management.enums.ComplaintStatus.REJECTED) AND c.slaDeadline < :now")
    long countSlaBreachedTotal(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Complaint c WHERE c.status NOT IN (com.hostel.management.enums.ComplaintStatus.RESOLVED_BY_MAINTENANCE, com.hostel.management.enums.ComplaintStatus.VERIFIED, com.hostel.management.enums.ComplaintStatus.CLOSED, com.hostel.management.enums.ComplaintStatus.REJECTED) AND c.slaDeadline < :now ORDER BY c.slaDeadline ASC")
    List<Complaint> findSlaBreachedComplaints(@Param("now") LocalDateTime now);

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
