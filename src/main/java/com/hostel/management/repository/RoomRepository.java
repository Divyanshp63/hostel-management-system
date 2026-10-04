package com.hostel.management.repository;

import com.hostel.management.entity.Room;
import com.hostel.management.enums.RoomStatus;
import com.hostel.management.enums.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);

    List<Room> findByStatus(RoomStatus status);

    long countByStatus(RoomStatus status);

    @Query("SELECT COALESCE(SUM(r.capacity), 0) FROM Room r")
    Long sumTotalCapacity();

    @Query("SELECT COALESCE(SUM(r.occupied), 0) FROM Room r")
    Long sumTotalOccupied();

    @Query("SELECT r.roomType, COUNT(r), COALESCE(SUM(r.capacity), 0), COALESCE(SUM(r.occupied), 0) " +
           "FROM Room r GROUP BY r.roomType")
    List<Object[]> getRoomOccupancyByType();

    @Query("SELECT r FROM Room r WHERE " +
            "(:search IS NULL OR :search = '' OR LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:roomType IS NULL OR r.roomType = :roomType)")
    Page<Room> findRoomsWithFilters(
            @Param("search") String search,
            @Param("status") RoomStatus status,
            @Param("roomType") RoomType roomType,
            Pageable pageable
    );
}
