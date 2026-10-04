package com.hostel.management.service;

import com.hostel.management.dto.request.CreateVisitorRequest;
import com.hostel.management.dto.request.VisitorApprovalRequest;
import com.hostel.management.dto.request.VisitorRejectionRequest;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.VisitorResponse;
import com.hostel.management.enums.VisitorStatus;

import java.time.LocalDate;
import java.util.List;

public interface VisitorService {

    VisitorResponse requestVisitorPass(CreateVisitorRequest request, String currentUserEmail);

    VisitorResponse approveVisitorRequest(Long id, VisitorApprovalRequest request);

    VisitorResponse rejectVisitorRequest(Long id, VisitorRejectionRequest request);

    VisitorResponse completeVisit(Long id);

    VisitorResponse cancelVisitorRequest(Long id, String currentUserEmail);

    VisitorResponse getVisitorRequestById(Long id, String currentUserEmail);

    PageResponse<VisitorResponse> getAllVisitorRequests(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            VisitorStatus status,
            LocalDate visitDate
    );

    List<VisitorResponse> getMyVisitorRequests(String currentUserEmail);

    void deleteVisitorRequest(Long id);
}
