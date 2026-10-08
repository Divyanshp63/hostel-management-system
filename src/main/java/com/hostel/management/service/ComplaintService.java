package com.hostel.management.service;

import com.hostel.management.dto.request.AssignComplaintRequest;
import com.hostel.management.dto.request.ComplaintFeedbackRequest;
import com.hostel.management.dto.request.CreateComplaintRequest;
import com.hostel.management.dto.request.UpdateComplaintStatusRequest;
import com.hostel.management.dto.response.ComplaintResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.ComplaintStatus;

import java.util.List;

public interface ComplaintService {

    ComplaintResponse createComplaint(CreateComplaintRequest request, String currentUserEmail);

    ComplaintResponse assignComplaint(Long id, AssignComplaintRequest request, String wardenEmail);

    ComplaintResponse acceptComplaint(Long id, String staffEmail);

    ComplaintResponse resolveComplaint(Long id, String resolutionRemarks, String staffEmail);

    ComplaintResponse verifyComplaintByWarden(Long id, String verificationRemarks, String wardenEmail);

    ComplaintResponse returnComplaintByWarden(Long id, String reason, String wardenEmail);

    List<ComplaintResponse> getComplaintsPendingVerification();

    ComplaintResponse submitFeedback(Long id, ComplaintFeedbackRequest request, String studentEmail);

    ComplaintResponse addWorkNotes(Long id, String notes, String staffEmail);

    ComplaintResponse updateComplaintStatus(Long id, UpdateComplaintStatusRequest request);

    ComplaintResponse getComplaintById(Long id, String currentUserEmail);

    List<ComplaintResponse> getComplaintsForStaff(String staffEmail);

    List<ComplaintResponse> getSlaBreachedComplaints();

    long countSlaBreached();

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
