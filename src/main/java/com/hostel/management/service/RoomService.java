package com.hostel.management.service;

import com.hostel.management.dto.request.CreateRoomRequest;
import com.hostel.management.dto.request.UpdateRoomRequest;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.RoomResponse;
import com.hostel.management.enums.RoomStatus;
import com.hostel.management.enums.RoomType;

import java.util.List;

public interface RoomService {

    RoomResponse createRoom(CreateRoomRequest request);

    RoomResponse updateRoom(Long id, UpdateRoomRequest request);

    RoomResponse getRoomById(Long id);

    RoomResponse getRoomByNumber(String roomNumber);

    PageResponse<RoomResponse> getAllRooms(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            RoomStatus status,
            RoomType roomType
    );

    List<RoomResponse> getAvailableRooms();

    void deleteRoom(Long id);
}
