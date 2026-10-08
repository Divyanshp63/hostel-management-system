package com.hostel.management.repository;

import com.hostel.management.entity.Staff;
import com.hostel.management.entity.User;
import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByUser(User user);

    boolean existsByUser(User user);

    Optional<Staff> findByUserId(Long userId);

    @Query("SELECT s FROM Staff s WHERE LOWER(s.user.email) = LOWER(:email)")
    Optional<Staff> findByUserEmail(@Param("email") String email);

    List<Staff> findByDepartment(ComplaintCategory department);

    @Query("SELECT s FROM Staff s WHERE s.user.role = :role")
    List<Staff> findByUserRole(@Param("role") Role role);
}
