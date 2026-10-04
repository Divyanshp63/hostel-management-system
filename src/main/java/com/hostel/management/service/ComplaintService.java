package com.hostel.management.service;

import com.hostel.management.dto.request.CreateComplaintRequest;
import com.hostel.management.dto.request.UpdateComplaintStatusRequest;
import com.hostel.management.dto.response.ComplaintResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.ComplaintStatus;

import java.util.List;

public interface ComplaintService {

    ComplaintResponse createComplaint(CreateComplaintRequest request, String currentUserEmail);

    ComplaintResponse updateComplaintStatus(Long id, UpdateComplaintStatusRequest request);

    ComplaintResponse getComplaintById(Long id, String currentUserEmail);

    PageResponse<ComplaintResponse> getAllComplaints(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            ComplaintStatus status,
            ComplaintCategory category
    );

    List<ComplaintResponse> getMyComplaints(String currentUserEmail);

    void deleteComplaint(Long id);
}
