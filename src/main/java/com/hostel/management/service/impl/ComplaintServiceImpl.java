package com.hostel.management.service.impl;

import com.hostel.management.dto.request.AssignComplaintRequest;
import com.hostel.management.dto.request.ComplaintFeedbackRequest;
import com.hostel.management.dto.request.CreateComplaintRequest;
import com.hostel.management.dto.request.UpdateComplaintStatusRequest;
import com.hostel.management.dto.response.ComplaintHistoryDto;
import com.hostel.management.dto.response.ComplaintResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.entity.*;
import com.hostel.management.enums.*;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.exception.UnauthorizedException;
import com.hostel.management.repository.*;
import com.hostel.management.service.AuditLogService;
import com.hostel.management.service.ComplaintService;
import com.hostel.management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final StudentRepository studentRepository;
    private final RoomAllocationRepository roomAllocationRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final AuditLogService auditLogService;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ComplaintResponse createComplaint(CreateComplaintRequest request, String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        Optional<RoomAllocation> activeAlloc = roomAllocationRepository
                .findByStudentIdAndStatus(student.getId(), AllocationStatus.ACTIVE);
        Room room = activeAlloc.map(RoomAllocation::getRoom).orElse(null);

        ComplaintPriority priority = request.getPriority() != null ? request.getPriority() : ComplaintPriority.MEDIUM;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime slaDeadline = now.plusHours(priority.getSlaHours());

        // Automated routing: status = SUBMITTED, department = MAINTENANCE
        Complaint complaint = Complaint.builder()
                .student(student)
                .room(room)
                .hostelName(student.getHostelName() != null ? student.getHostelName() : (room != null ? room.getBlockName() : "Main Hostel"))
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .category(request.getCategory() != null ? request.getCategory() : ComplaintCategory.MAINTENANCE)
                .priority(priority)
                .status(ComplaintStatus.SUBMITTED)
                .assignedDepartment(ComplaintCategory.MAINTENANCE)
                .slaDeadline(slaDeadline)
                .build();

        Complaint saved = complaintRepository.save(complaint);

        auditLogService.log(
                student.getUser().getId(),
                student.getUser().getName(),
                "STUDENT",
                "COMPLAINT_SUBMITTED",
                "COMPLAINT",
                saved.getComplaintCode(),
                "Submitted complaint: " + saved.getTitle() + " (" + saved.getCategory() + ", " + saved.getPriority() + ")"
        );

        // Notify Maintenance Department
        notificationService.notifyMaintenance(
                "New Complaint Submitted",
                "Ticket #" + saved.getComplaintCode() + ": " + saved.getTitle() + " (" + saved.getPriority() + " priority)",
                saved.getId()
        );

        log.info("Complaint {} auto-routed to MAINTENANCE for student {}", saved.getComplaintCode(), student.getUser().getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ComplaintResponse assignComplaint(Long id, AssignComplaintRequest request, String wardenEmail) {
        // Old assignment logic deprecated. All complaints automatically routed to MAINTENANCE.
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        complaint.setAssignedDepartment(ComplaintCategory.MAINTENANCE);
        if (request.getPriority() != null) {
            complaint.setPriority(request.getPriority());
        }
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            complaint.setWorkNotes(request.getRemarks().trim());
        }
        Complaint saved = complaintRepository.save(complaint);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ComplaintResponse acceptComplaint(Long id, String staffEmail) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        User staffUser = userRepository.findByEmail(staffEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found: " + staffEmail));

        LocalDateTime now = LocalDateTime.now();
        complaint.setAssignedStaff(staffUser);
        complaint.setAcceptedAt(now);
        complaint.setStartedAt(now);
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);

        Complaint saved = complaintRepository.save(complaint);

        auditLogService.log(
                staffUser.getId(),
                staffUser.getName(),
                "MAINTENANCE",
                "COMPLAINT_ACCEPTED",
                "COMPLAINT",
                saved.getComplaintCode(),
                "Complaint ticket accepted and work started by " + staffUser.getName()
        );

        log.info("Complaint {} accepted by maintenance staff {}", saved.getComplaintCode(), staffUser.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ComplaintResponse resolveComplaint(Long id, String resolutionRemarks, String staffEmail) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        if (resolutionRemarks == null || resolutionRemarks.isBlank()) {
            throw new BadRequestException("Resolution details are required to mark the complaint as resolved.");
        }

        User staffUser = userRepository.findByEmail(staffEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found: " + staffEmail));

        LocalDateTime now = LocalDateTime.now();
        if (complaint.getAssignedStaff() == null) {
            complaint.setAssignedStaff(staffUser);
        }
        complaint.setStatus(ComplaintStatus.RESOLVED_BY_MAINTENANCE);
        complaint.setResolutionDetails(resolutionRemarks.trim());
        complaint.setResolutionRemarks(resolutionRemarks.trim());
        complaint.setResolvedAt(now);

        Complaint saved = complaintRepository.save(complaint);

        auditLogService.log(
                staffUser.getId(),
                staffUser.getName(),
                "MAINTENANCE",
                "COMPLAINT_RESOLVED",
                "COMPLAINT",
                saved.getComplaintCode(),
                "Resolved by Maintenance: " + resolutionRemarks.trim()
        );

        // Notify Warden for verification
        notificationService.notifyWarden(
                "Complaint Verification Required",
                "Complaint #" + saved.getComplaintCode() + " (" + saved.getTitle() + ") is ready for verification.",
                saved.getId()
        );

        log.info("Complaint {} marked RESOLVED_BY_MAINTENANCE by staff {}", saved.getComplaintCode(), staffUser.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ComplaintResponse verifyComplaintByWarden(Long id, String verificationRemarks, String wardenEmail) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        if (complaint.getStatus() != ComplaintStatus.RESOLVED_BY_MAINTENANCE) {
            throw new BadRequestException("Only complaints marked RESOLVED_BY_MAINTENANCE can be verified by Warden.");
        }

        User warden = userRepository.findByEmail(wardenEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Warden user not found: " + wardenEmail));

        String remarks = (verificationRemarks != null && !verificationRemarks.isBlank())
                ? verificationRemarks.trim()
                : "Problem inspected, verified, and confirmed resolved by Warden.";

        LocalDateTime now = LocalDateTime.now();
        complaint.setVerifiedAt(now);
        complaint.setVerifiedBy(warden);
        complaint.setVerifiedByName(warden.getName());
        complaint.setVerificationRemarks(remarks);
        complaint.setStatus(ComplaintStatus.CLOSED);

        Complaint saved = complaintRepository.save(complaint);

        auditLogService.log(
                warden.getId(),
                warden.getName(),
                "WARDEN",
                "COMPLAINT_VERIFIED",
                "COMPLAINT",
                saved.getComplaintCode(),
                "Verified and Closed by Warden: " + remarks
        );

        // Notify Student
        if (saved.getStudent() != null && saved.getStudent().getUser() != null) {
            notificationService.notifyStudent(
                    saved.getStudent().getUser().getId(),
                    "Complaint Verified & Closed",
                    "Your complaint #" + saved.getComplaintCode() + " has been verified by the Warden and closed.",
                    saved.getId()
            );
        }

        log.info("Complaint {} verified and CLOSED by warden {}", saved.getComplaintCode(), warden.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ComplaintResponse returnComplaintByWarden(Long id, String reason, String wardenEmail) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        if (complaint.getStatus() != ComplaintStatus.RESOLVED_BY_MAINTENANCE) {
            throw new BadRequestException("Only complaints in RESOLVED_BY_MAINTENANCE can be returned to Maintenance.");
        }

        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("A return reason is required when rejecting verification.");
        }

        User warden = userRepository.findByEmail(wardenEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Warden user not found: " + wardenEmail));

        complaint.setStatus(ComplaintStatus.RETURNED);
        complaint.setVerificationRemarks(reason.trim());

        Complaint saved = complaintRepository.save(complaint);

        auditLogService.log(
                warden.getId(),
                warden.getName(),
                "WARDEN",
                "COMPLAINT_RETURNED",
                "COMPLAINT",
                saved.getComplaintCode(),
                "Returned to Maintenance: " + reason.trim()
        );

        // Notify Maintenance Department
        notificationService.notifyMaintenance(
                "Complaint Returned for Rework",
                "Ticket #" + saved.getComplaintCode() + " returned by Warden: " + reason.trim(),
                saved.getId()
        );

        log.info("Complaint {} returned to maintenance by warden {}: {}", saved.getComplaintCode(), warden.getName(), reason.trim());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsPendingVerification() {
        return complaintRepository.findByStatusOrderByCreatedAtDesc(ComplaintStatus.RESOLVED_BY_MAINTENANCE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ComplaintResponse submitFeedback(Long id, ComplaintFeedbackRequest request, String studentEmail) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        User studentUser = userRepository.findByEmail(studentEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student user not found: " + studentEmail));

        if (!complaint.getStudent().getUser().getId().equals(studentUser.getId())) {
            throw new UnauthorizedException("You can only submit feedback for your own complaints.");
        }

        if (complaint.getStatus() != ComplaintStatus.CLOSED && complaint.getStatus() != ComplaintStatus.VERIFIED) {
            throw new BadRequestException("Feedback can only be submitted once the complaint is verified and closed.");
        }

        complaint.setRating(request.getRating());
        complaint.setFeedback(request.getFeedback().trim());
        complaint.setFeedbackAt(LocalDateTime.now());

        Complaint saved = complaintRepository.save(complaint);

        auditLogService.log(
                studentUser.getId(),
                studentUser.getName(),
                "STUDENT",
                "COMPLAINT_FEEDBACK",
                "COMPLAINT",
                saved.getComplaintCode(),
                "Rating: " + request.getRating() + "/5 stars. Feedback: " + request.getFeedback()
        );

        log.info("Complaint {} feedback recorded by student {}", saved.getComplaintCode(), studentUser.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ComplaintResponse addWorkNotes(Long id, String notes, String staffEmail) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        User staffUser = userRepository.findByEmail(staffEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found: " + staffEmail));

        String existingNotes = complaint.getWorkNotes() != null ? complaint.getWorkNotes() : "";
        String timestamp = LocalDateTime.now().toString().substring(0, 16);
        String updatedNotes = existingNotes + "\n[" + timestamp + " - " + staffUser.getName() + "]: " + notes.trim();
        complaint.setWorkNotes(updatedNotes.trim());

        Complaint saved = complaintRepository.save(complaint);

        auditLogService.log(
                staffUser.getId(),
                staffUser.getName(),
                "MAINTENANCE",
                "COMPLAINT_NOTE_ADDED",
                "COMPLAINT",
                saved.getComplaintCode(),
                notes.trim()
        );

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaintStatus(Long id, UpdateComplaintStatusRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        ComplaintStatus newStatus = request.getStatus();
        if (newStatus == ComplaintStatus.RESOLVED_BY_MAINTENANCE) {
            complaint.setResolvedAt(LocalDateTime.now());
        }
        if (request.getResolutionRemarks() != null && !request.getResolutionRemarks().isBlank()) {
            complaint.setResolutionRemarks(request.getResolutionRemarks().trim());
            complaint.setResolutionDetails(request.getResolutionRemarks().trim());
        }

        complaint.setStatus(newStatus);
        Complaint updated = complaintRepository.save(complaint);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsForStaff(String staffEmail) {
        User staffUser = userRepository.findByEmail(staffEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found: " + staffEmail));

        Optional<Staff> staffOpt = staffRepository.findByUser(staffUser);
        ComplaintCategory department = staffOpt.map(Staff::getDepartment).orElse(ComplaintCategory.MAINTENANCE);

        List<Complaint> list = complaintRepository.findForStaffMember(staffUser.getId(), department);
        return list.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getSlaBreachedComplaints() {
        return complaintRepository.findSlaBreachedComplaints(LocalDateTime.now()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countSlaBreached() {
        return complaintRepository.countSlaBreachedTotal(LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id, String currentUserEmail) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        if (currentUser.getRole() == Role.STUDENT &&
                !complaint.getStudent().getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this complaint");
        }

        return mapToResponse(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ComplaintResponse> getAllComplaints(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            ComplaintStatus status,
            ComplaintCategory category
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Complaint> complaintPage = complaintRepository.findComplaintsWithFilters(
                search != null ? search.trim() : "",
                status,
                category,
                pageable
        );

        List<ComplaintResponse> responses = complaintPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<ComplaintResponse>builder()
                .content(responses)
                .pageNumber(complaintPage.getNumber())
                .pageSize(complaintPage.getSize())
                .totalElements(complaintPage.getTotalElements())
                .totalPages(complaintPage.getTotalPages())
                .last(complaintPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getMyComplaints(String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));

        return complaintRepository.findByStudentIdOrderByCreatedAtDesc(student.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteComplaint(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));

        complaintRepository.delete(complaint);
    }

    private ComplaintResponse mapToResponse(Complaint c) {
        Student student = c.getStudent();
        User studentUser = student != null ? student.getUser() : null;

        // Fetch audit history timeline
        List<AuditLog> logs = auditLogRepository.findByModuleAndRecordIdInOrderByCreatedAtAsc(
                "COMPLAINT",
                List.of(c.getComplaintCode(), String.valueOf(c.getId()))
        );

        List<ComplaintHistoryDto> historyList = new ArrayList<>();
        for (AuditLog logEntry : logs) {
            String actionName = mapActionName(logEntry.getAction());
            historyList.add(ComplaintHistoryDto.builder()
                    .action(actionName)
                    .performedBy(logEntry.getUserName() != null ? logEntry.getUserName() : "System")
                    .role(logEntry.getUserRole() != null ? logEntry.getUserRole() : "")
                    .timestamp(logEntry.getCreatedAt())
                    .remarks(logEntry.getRemarks())
                    .build());
        }

        // If no explicit audit logs found (e.g. legacy data), generate synthetic history from entity fields
        if (historyList.isEmpty()) {
            if (c.getCreatedAt() != null) {
                historyList.add(ComplaintHistoryDto.builder()
                        .action("Complaint Submitted")
                        .performedBy(studentUser != null ? studentUser.getName() : "Student")
                        .role("STUDENT")
                        .timestamp(c.getCreatedAt())
                        .remarks("Issue: " + c.getTitle())
                        .build());
            }
            if (c.getAcceptedAt() != null) {
                historyList.add(ComplaintHistoryDto.builder()
                        .action("Complaint Accepted & Started")
                        .performedBy(c.getAssignedStaff() != null ? c.getAssignedStaff().getName() : "Maintenance Staff")
                        .role("MAINTENANCE")
                        .timestamp(c.getAcceptedAt())
                        .remarks("Work in progress")
                        .build());
            }
            if (c.getResolvedAt() != null) {
                historyList.add(ComplaintHistoryDto.builder()
                        .action("Resolved by Maintenance")
                        .performedBy(c.getAssignedStaff() != null ? c.getAssignedStaff().getName() : "Maintenance Staff")
                        .role("MAINTENANCE")
                        .timestamp(c.getResolvedAt())
                        .remarks(c.getEffectiveResolutionDetails())
                        .build());
            }
            if (c.getVerifiedAt() != null) {
                historyList.add(ComplaintHistoryDto.builder()
                        .action("Verified & Closed by Warden")
                        .performedBy(c.getVerifiedByName() != null ? c.getVerifiedByName() : "Warden")
                        .role("WARDEN")
                        .timestamp(c.getVerifiedAt())
                        .remarks(c.getEffectiveVerificationRemarks())
                        .build());
            }
        }

        return ComplaintResponse.builder()
                .id(c.getId())
                .complaintCode(c.getComplaintCode())
                .studentId(student != null ? student.getId() : null)
                .studentName(studentUser != null ? studentUser.getName() : "Unknown")
                .studentAdmissionNumber(student != null ? student.getAdmissionNumber() : "N/A")
                .studentEmail(studentUser != null ? studentUser.getEmail() : "")
                .studentPhone(studentUser != null ? studentUser.getPhone() : "")
                .roomNumber(c.getRoom() != null ? c.getRoom().getRoomNumber() : "N/A")
                .hostelName(c.getHostelName())
                .title(c.getTitle())
                .description(c.getDescription())
                .category(c.getCategory())
                .priority(c.getPriority())
                .status(c.getStatus())
                .assignedDepartment(c.getAssignedDepartment())
                .assignedStaffId(c.getAssignedStaff() != null ? c.getAssignedStaff().getId() : null)
                .assignedStaffName(c.getAssignedStaff() != null ? c.getAssignedStaff().getName() : null)
                .assignedAt(c.getAssignedAt())
                .acceptedAt(c.getAcceptedAt())
                .startedAt(c.getStartedAt())
                .slaDeadline(c.getSlaDeadline())
                .slaBreached(c.isSlaBreached())
                .resolutionRemarks(c.getResolutionRemarks())
                .resolutionDetails(c.getEffectiveResolutionDetails())
                .resolvedAt(c.getResolvedAt())
                .verifiedAt(c.getVerifiedAt())
                .verifiedById(c.getVerifiedBy() != null ? c.getVerifiedBy().getId() : null)
                .verifiedByName(c.getVerifiedByName())
                .verificationRemarks(c.getEffectiveVerificationRemarks())
                .workNotes(c.getWorkNotes())
                .rating(c.getRating())
                .feedback(c.getFeedback())
                .feedbackAt(c.getFeedbackAt())
                .history(historyList)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private String mapActionName(String rawAction) {
        if (rawAction == null) return "Action Performed";
        return switch (rawAction) {
            case "COMPLAINT_SUBMITTED" -> "Complaint Submitted";
            case "COMPLAINT_ACCEPTED" -> "Complaint Accepted & Work Started";
            case "COMPLAINT_RESOLVED" -> "Marked Resolved by Maintenance";
            case "COMPLAINT_VERIFIED" -> "Verified & Closed by Warden";
            case "COMPLAINT_RETURNED" -> "Returned to Maintenance by Warden";
            case "COMPLAINT_NOTE_ADDED" -> "Work Note Added";
            case "COMPLAINT_FEEDBACK" -> "Student Rating & Feedback";
            default -> rawAction.replace('_', ' ');
        };
    }
}
