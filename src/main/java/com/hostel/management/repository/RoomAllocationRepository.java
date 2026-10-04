package com.hostel.management.repository;

import com.hostel.management.entity.RoomAllocation;
import com.hostel.management.enums.AllocationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomAllocationRepository extends JpaRepository<RoomAllocation, Long> {

    Optional<RoomAllocation> findByStudentIdAndStatus(Long studentId, AllocationStatus status);

    boolean existsByStudentIdAndStatus(Long studentId, AllocationStatus status);

    List<RoomAllocation> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<RoomAllocation> findByRoomIdAndStatus(Long roomId, AllocationStatus status);

    long countByStatus(AllocationStatus status);

    List<RoomAllocation> findTop5ByStatusOrderByCreatedAtDesc(AllocationStatus status);

    @Query("SELECT a FROM RoomAllocation a JOIN a.student s JOIN s.user u JOIN a.room r WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.admissionNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR a.status = :status)")
    Page<RoomAllocation> findAllocationsWithFilters(
            @Param("search") String search,
            @Param("status") AllocationStatus status,
            Pageable pageable
    );
}
