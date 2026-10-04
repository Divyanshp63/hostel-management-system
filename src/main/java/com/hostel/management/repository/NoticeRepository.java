package com.hostel.management.repository;

import com.hostel.management.entity.Notice;
import com.hostel.management.enums.NoticePriority;
import com.hostel.management.enums.NoticeTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByActiveTrueOrderByCreatedAtDesc();

    List<Notice> findTop3ByActiveTrueOrderByCreatedAtDesc();

    long countByActiveTrue();

    @Query("SELECT n FROM Notice n WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:priority IS NULL OR n.priority = :priority) AND " +
            "(:target IS NULL OR n.targetAudience = :target) AND " +
            "(:active IS NULL OR n.active = :active)")
    Page<Notice> findNoticesWithFilters(
            @Param("search") String search,
            @Param("priority") NoticePriority priority,
            @Param("target") NoticeTarget target,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
