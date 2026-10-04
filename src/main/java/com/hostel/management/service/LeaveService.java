package com.hostel.management.service;

import com.hostel.management.dto.request.CreateLeaveRequest;
import com.hostel.management.dto.request.LeaveActionRequest;
import com.hostel.management.dto.response.LeaveResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.enums.LeaveStatus;
import com.hostel.management.enums.LeaveType;

import java.util.List;

public interface LeaveService {

    LeaveResponse applyLeave(CreateLeaveRequest request, String currentUserEmail);

    LeaveResponse approveLeave(Long id, LeaveActionRequest request);

    LeaveResponse rejectLeave(Long id, LeaveActionRequest request);

    LeaveResponse cancelLeave(Long id, String currentUserEmail);

    LeaveResponse getLeaveById(Long id, String currentUserEmail);

    PageResponse<LeaveResponse> getAllLeaves(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            LeaveStatus status,
            LeaveType leaveType
    );

    List<LeaveResponse> getMyLeaves(String currentUserEmail);

    void deleteLeave(Long id);
}
