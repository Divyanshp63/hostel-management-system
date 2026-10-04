package com.hostel.management.repository;

import com.hostel.management.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByUserEmail(String email);

    Optional<Student> findByAdmissionNumber(String admissionNumber);

    boolean existsByAdmissionNumber(String admissionNumber);

    boolean existsByUserId(Long userId);

    @Query("SELECT s FROM Student s JOIN s.user u WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(s.admissionNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.course) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Student> searchStudents(@Param("search") String search, Pageable pageable);
}
