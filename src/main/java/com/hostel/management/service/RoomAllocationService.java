package com.hostel.management.service;

import com.hostel.management.dto.request.AdminAllocationRequest;
import com.hostel.management.dto.request.ApprovalRequest;
import com.hostel.management.dto.request.RejectionRequest;
import com.hostel.management.dto.request.RoomAllocationRequest;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.RoomAllocationResponse;
import com.hostel.management.enums.AllocationStatus;

import java.util.List;

public interface RoomAllocationService {

    RoomAllocationResponse requestAllocation(RoomAllocationRequest request, String currentUserEmail);

    RoomAllocationResponse directAllocate(AdminAllocationRequest request);

    RoomAllocationResponse approveAllocation(Long allocationId, ApprovalRequest request);

    RoomAllocationResponse rejectAllocation(Long allocationId, RejectionRequest request);

    RoomAllocationResponse vacateAllocation(Long allocationId);

    RoomAllocationResponse cancelAllocationRequest(Long allocationId, String currentUserEmail);

    PageResponse<RoomAllocationResponse> getAllAllocations(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            AllocationStatus status
    );

    List<RoomAllocationResponse> getMyAllocations(String currentUserEmail);

    RoomAllocationResponse getAllocationById(Long id, String currentUserEmail);
}
