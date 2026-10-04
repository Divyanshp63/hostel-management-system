package com.hostel.management.service.impl;

import com.hostel.management.dto.request.CreateRoomRequest;
import com.hostel.management.dto.request.UpdateRoomRequest;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.RoomResponse;
import com.hostel.management.entity.Room;
import com.hostel.management.enums.RoomStatus;
import com.hostel.management.enums.RoomType;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.DuplicateResourceException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        String normalizedRoomNumber = request.getRoomNumber().trim().toUpperCase();

        if (roomRepository.existsByRoomNumber(normalizedRoomNumber)) {
            throw new DuplicateResourceException("Room with number " + normalizedRoomNumber + " already exists");
        }

        Room room = Room.builder()
                .roomNumber(normalizedRoomNumber)
                .floor(request.getFloor())
                .roomType(request.getRoomType())
                .capacity(request.getCapacity())
                .occupied(0)
                .rentPerMonth(request.getRentPerMonth())
                .status(RoomStatus.AVAILABLE)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .build();

        Room savedRoom = roomRepository.save(room);
        return mapToResponse(savedRoom);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));

        // Business Rule: Capacity cannot be less than currently occupied beds
        if (request.getCapacity() < room.getOccupied()) {
            throw new BadRequestException("Capacity (" + request.getCapacity() +
                    ") cannot be less than currently occupied beds (" + room.getOccupied() + ")");
        }

        // Business Rule: Cannot put room UNDER_MAINTENANCE if students are currently occupying it
        if (request.getStatus() == RoomStatus.UNDER_MAINTENANCE && room.getOccupied() > 0) {
            throw new BadRequestException("Cannot set room to UNDER_MAINTENANCE while " +
                    room.getOccupied() + " bed(s) are actively occupied");
        }

        room.setFloor(request.getFloor());
        room.setRoomType(request.getRoomType());
        room.setCapacity(request.getCapacity());
        room.setRentPerMonth(request.getRentPerMonth());
        room.setStatus(request.getStatus());
        room.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        // Auto calculate status based on capacity & occupied unless it is explicitly under maintenance
        room.updateStatusBasedOnOccupancy();

        Room updatedRoom = roomRepository.save(room);
        return mapToResponse(updatedRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));
        return mapToResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomByNumber(String roomNumber) {
        Room room = roomRepository.findByRoomNumber(roomNumber.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with number: " + roomNumber));
        return mapToResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoomResponse> getAllRooms(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            RoomStatus status,
            RoomType roomType
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Room> roomPage = roomRepository.findRoomsWithFilters(
                search != null ? search.trim() : "",
                status,
                roomType,
                pageable
        );

        List<RoomResponse> responses = roomPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<RoomResponse>builder()
                .content(responses)
                .pageNumber(roomPage.getNumber())
                .pageSize(roomPage.getSize())
                .totalElements(roomPage.getTotalElements())
                .totalPages(roomPage.getTotalPages())
                .last(roomPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms() {
        return roomRepository.findByStatus(RoomStatus.AVAILABLE).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));

        // Business Rule: A room cannot be deleted if it has active occupants
        if (room.getOccupied() > 0) {
            throw new BadRequestException("Cannot delete room " + room.getRoomNumber() +
                    " because it currently has " + room.getOccupied() + " active resident(s)");
        }

        roomRepository.delete(room);
    }

    private RoomResponse mapToResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .floor(room.getFloor())
                .roomType(room.getRoomType())
                .capacity(room.getCapacity())
                .occupied(room.getOccupied())
                .availableBeds(room.getAvailableBeds())
                .rentPerMonth(room.getRentPerMonth())
                .status(room.getStatus())
                .description(room.getDescription())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
