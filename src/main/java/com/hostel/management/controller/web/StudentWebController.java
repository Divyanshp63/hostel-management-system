package com.hostel.management.controller.web;

import com.hostel.management.dto.request.*;
import com.hostel.management.dto.response.*;
import com.hostel.management.entity.*;
import com.hostel.management.enums.*;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.*;
import com.hostel.management.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
@Slf4j
public class StudentWebController {

    private final DashboardService dashboardService;
    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final RoomAllocationService roomAllocationService;
    private final RoomAllocationRepository roomAllocationRepository;
    private final FeeService feeService;
    private final FeeRepository feeRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final ComplaintService complaintService;
    private final ComplaintRepository complaintRepository;
    private final LeaveService leaveService;
    private final LeaveRequestRepository leaveRequestRepository;
    private final VisitorService visitorService;
    private final VisitorRequestRepository visitorRequestRepository;
    private final NoticeRepository noticeRepository;
    private final MessMenuService messMenuService;

    // Helper to get student entity
    private Student getAuthenticatedStudent(Authentication auth) {
        return studentRepository.findByUserEmail(auth.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + auth.getName()));
    }

    // ==========================================
    // 1. STUDENT DASHBOARD
    // ==========================================
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String email = auth.getName();
        StudentDashboardResponse stats = dashboardService.getStudentDashboardStats(email);
        Student student = getAuthenticatedStudent(auth);

        model.addAttribute("stats", stats);
        model.addAttribute("student", student);
        model.addAttribute("activeAllocation", roomAllocationRepository.findByStudentIdAndStatus(student.getId(), AllocationStatus.ACTIVE).orElse(null));
        model.addAttribute("recentComplaints", complaintRepository.findByStudentIdOrderByCreatedAtDesc(student.getId()));
        model.addAttribute("recentLeaves", leaveRequestRepository.findByStudentIdOrderByCreatedAtDesc(student.getId()));
        model.addAttribute("notices", noticeRepository.findTop3ByActiveTrueOrderByCreatedAtDesc());
        return "student/dashboard";
    }

    // ==========================================
    // 2. PROFILE MANAGEMENT
    // ==========================================
    @GetMapping("/profile")
    public String viewProfile(Authentication auth, Model model) {
        Student student = getAuthenticatedStudent(auth);
        model.addAttribute("student", student);
        return "student/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Authentication auth, Model model) {
        Student student = getAuthenticatedStudent(auth);
        UpdateStudentRequest req = UpdateStudentRequest.builder()
                .name(student.getUser().getName())
                .phone(student.getUser().getPhone())
                .course(student.getCourse())
                .yearOfStudy(student.getYearOfStudy())
                .gender(student.getGender())
                .dateOfBirth(student.getDateOfBirth())
                .bloodGroup(student.getBloodGroup())
                .address(student.getAddress())
                .guardianName(student.getGuardianName())
                .guardianPhone(student.getGuardianPhone())
                .emergencyContact(student.getEmergencyContact())
                .build();

        model.addAttribute("student", req);
        model.addAttribute("studentId", student.getId());
        model.addAttribute("admissionNumber", student.getAdmissionNumber());
        model.addAttribute("email", student.getUser().getEmail());
        model.addAttribute("genders", Gender.values());
        return "student/profile-edit";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @Valid @ModelAttribute("student") UpdateStudentRequest request,
            BindingResult bindingResult,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Student student = getAuthenticatedStudent(auth);
        if (bindingResult.hasErrors()) {
            model.addAttribute("studentId", student.getId());
            model.addAttribute("admissionNumber", student.getAdmissionNumber());
            model.addAttribute("email", student.getUser().getEmail());
            model.addAttribute("genders", Gender.values());
            return "student/profile-edit";
        }
        try {
            studentService.updateStudent(student.getId(), request, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/student/profile";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("genders", Gender.values());
            return "student/profile-edit";
        }
    }

    // ==========================================
    // 3. ASSIGNED ROOM & ALLOCATION REQUEST
    // ==========================================
    @GetMapping("/room")
    public String viewRoom(Authentication auth, Model model) {
        Student student = getAuthenticatedStudent(auth);
        RoomAllocation activeAlloc = roomAllocationRepository.findByStudentIdAndStatus(student.getId(), AllocationStatus.ACTIVE).orElse(null);
        RoomAllocation pendingAlloc = roomAllocationRepository.findByStudentIdAndStatus(student.getId(), AllocationStatus.PENDING).orElse(null);

        model.addAttribute("student", student);
        model.addAttribute("activeAllocation", activeAlloc);
        model.addAttribute("pendingAllocation", pendingAlloc);

        if (activeAlloc != null) {
            // Find roommates
            List<RoomAllocation> roommates = roomAllocationRepository.findByRoomIdAndStatus(activeAlloc.getRoom().getId(), AllocationStatus.ACTIVE)
                    .stream()
                    .filter(a -> !a.getStudent().getId().equals(student.getId()))
                    .toList();
            model.addAttribute("roommates", roommates);
        } else {
            model.addAttribute("availableRooms", roomRepository.findByStatus(RoomStatus.AVAILABLE));
        }

        return "student/room";
    }

    @PostMapping("/room/request")
    public String requestRoomAllocation(
            @RequestParam("roomId") Long roomId,
            @RequestParam(value = "remarks", required = false) String remarks,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            RoomAllocationRequest req = RoomAllocationRequest.builder()
                    .roomId(roomId)
                    .remarks(remarks)
                    .build();
            roomAllocationService.requestAllocation(req, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Room allocation requested successfully! Awaiting Warden approval.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/room";
    }

    // ==========================================
    // 4. FEE STATUS & PAYMENT HISTORY
    // ==========================================
    @GetMapping("/fees")
    public String viewFees(Authentication auth, Model model) {
        Student student = getAuthenticatedStudent(auth);
        List<Fee> fees = feeRepository.findByStudentIdOrderByDueDateDesc(student.getId());
        List<Payment> payments = paymentRepository.findByStudentIdOrderByPaymentDateDesc(student.getId());
        BigDecimal totalPending = feeRepository.sumRemainingByStudentId(student.getId());

        model.addAttribute("fees", fees);
        model.addAttribute("payments", payments);
        model.addAttribute("totalPending", totalPending != null ? totalPending : BigDecimal.ZERO);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "student/fees";
    }

    @PostMapping("/fees/{id}/pay")
    public String processPayment(
            @PathVariable Long id,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("paymentMethod") PaymentMethod paymentMethod,
            @RequestParam(value = "remarks", required = false) String remarks,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            PaymentRequest req = PaymentRequest.builder()
                    .feeId(id)
                    .amount(amount)
                    .paymentMethod(paymentMethod)
                    .remarks(remarks != null && !remarks.isBlank() ? remarks : "Online Student Payment")
                    .build();
            PaymentResponse response = paymentService.processPayment(req, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment of ₹" + amount + " processed successfully! Reference ID: " + response.getTransactionId());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/fees";
    }

    // ==========================================
    // 5. COMPLAINT MANAGEMENT
    // ==========================================
    @GetMapping("/complaints")
    public String viewComplaints(Authentication auth, Model model) {
        if (auth == null || auth.getName() == null) {
            return "redirect:/login";
        }
        Student student = getAuthenticatedStudent(auth);
        List<ComplaintResponse> complaints = complaintService.getMyComplaints(auth.getName());
        model.addAttribute("complaints", complaints != null ? complaints : java.util.Collections.emptyList());

        long myComplaintsCount = complaintRepository.countByStudentId(student.getId());
        long openComplaintsCount = complaintRepository.countByStudentIdAndStatus(student.getId(), ComplaintStatus.SUBMITTED);
        long inProgressCount = complaintRepository.countByStudentIdAndStatusIn(student.getId(), List.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.RETURNED));
        long resolvedCount = complaintRepository.countByStudentIdAndStatus(student.getId(), ComplaintStatus.RESOLVED_BY_MAINTENANCE);
        long closedCount = complaintRepository.countByStudentIdAndStatusIn(student.getId(), List.of(ComplaintStatus.CLOSED, ComplaintStatus.VERIFIED));

        model.addAttribute("myComplaintsCount", myComplaintsCount);
        model.addAttribute("openComplaintsCount", openComplaintsCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("closedCount", closedCount);
        return "student/complaints";
    }

    @GetMapping("/complaints/new")
    public String showComplaintForm(Authentication auth, Model model) {
        if (auth == null || auth.getName() == null) {
            return "redirect:/login";
        }
        Student student = getAuthenticatedStudent(auth);
        RoomAllocation alloc = roomAllocationRepository.findByStudentIdAndStatus(student.getId(), AllocationStatus.ACTIVE).orElse(null);

        CreateComplaintRequest req = new CreateComplaintRequest();
        model.addAttribute("complaint", req);
        model.addAttribute("categories", ComplaintCategory.values());
        model.addAttribute("priorities", ComplaintPriority.values());
        model.addAttribute("currentRoom", alloc != null ? alloc.getRoom().getRoomNumber() : "None Assigned");
        return "student/complaint-form";
    }

    @PostMapping("/complaints/save")
    public String saveComplaint(
            @Valid @ModelAttribute("complaint") CreateComplaintRequest request,
            BindingResult bindingResult,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (auth == null || auth.getName() == null) {
            return "redirect:/login";
        }
        Student student = getAuthenticatedStudent(auth);
        RoomAllocation alloc = roomAllocationRepository.findByStudentIdAndStatus(student.getId(), AllocationStatus.ACTIVE).orElse(null);

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", ComplaintCategory.values());
            model.addAttribute("priorities", ComplaintPriority.values());
            model.addAttribute("currentRoom", alloc != null ? alloc.getRoom().getRoomNumber() : "None Assigned");
            return "student/complaint-form";
        }
        try {
            complaintService.createComplaint(request, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Complaint submitted successfully! Automatically routed to Maintenance Department.");
            return "redirect:/student/complaints";
        } catch (Exception ex) {
            model.addAttribute("categories", ComplaintCategory.values());
            model.addAttribute("priorities", ComplaintPriority.values());
            model.addAttribute("currentRoom", alloc != null ? alloc.getRoom().getRoomNumber() : "None Assigned");
            model.addAttribute("errorMessage", ex.getMessage());
            return "student/complaint-form";
        }
    }

    @PostMapping("/complaints/feedback/{id}")
    public String submitComplaintFeedback(
            @PathVariable Long id,
            @RequestParam("rating") Integer rating,
            @RequestParam("feedback") String feedback,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        if (auth == null || auth.getName() == null) {
            return "redirect:/login";
        }
        try {
            ComplaintFeedbackRequest req = ComplaintFeedbackRequest.builder()
                    .rating(rating)
                    .feedback(feedback)
                    .build();
            complaintService.submitFeedback(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for your feedback! The complaint ticket is now CLOSED.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/complaints";
    }

    // ==========================================
    // 6. LEAVE MANAGEMENT
    // ==========================================
    @GetMapping("/leaves")
    public String viewLeaves(Authentication auth, Model model) {
        Student student = getAuthenticatedStudent(auth);
        model.addAttribute("leaves", leaveRequestRepository.findByStudentIdOrderByCreatedAtDesc(student.getId()));
        return "student/leaves";
    }

    @GetMapping("/leaves/new")
    public String showLeaveForm(Authentication auth, Model model) {
        Student student = getAuthenticatedStudent(auth);
        CreateLeaveRequest req = CreateLeaveRequest.builder()
                .emergencyContactPhone(student.getEmergencyContact())
                .fromDate(LocalDate.now().plusDays(1))
                .toDate(LocalDate.now().plusDays(2))
                .build();

        model.addAttribute("leave", req);
        model.addAttribute("leaveTypes", LeaveType.values());
        return "student/leave-form";
    }

    @PostMapping("/leaves/save")
    public String saveLeave(
            @Valid @ModelAttribute("leave") CreateLeaveRequest request,
            BindingResult bindingResult,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (request.getFromDate() != null && request.getToDate() != null && request.getFromDate().isAfter(request.getToDate())) {
            bindingResult.rejectValue("toDate", "error.toDate", "To Date cannot be earlier than From Date");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("leaveTypes", LeaveType.values());
            return "student/leave-form";
        }
        try {
            leaveService.applyLeave(request, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Leave pass request submitted successfully!");
            return "redirect:/student/leaves";
        } catch (Exception ex) {
            model.addAttribute("leaveTypes", LeaveType.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "student/leave-form";
        }
    }

    // ==========================================
    // 7. VISITOR MANAGEMENT
    // ==========================================
    @GetMapping("/visitors")
    public String viewVisitors(Authentication auth, Model model) {
        Student student = getAuthenticatedStudent(auth);
        model.addAttribute("visitors", visitorRequestRepository.findByStudentIdOrderByVisitDateDesc(student.getId()));
        return "student/visitors";
    }

    @GetMapping("/visitors/new")
    public String showVisitorForm(Model model) {
        CreateVisitorRequest req = CreateVisitorRequest.builder()
                .visitDate(LocalDate.now().plusDays(1))
                .build();
        model.addAttribute("visitor", req);
        return "student/visitor-form";
    }

    @PostMapping("/visitors/save")
    public String saveVisitor(
            @Valid @ModelAttribute("visitor") CreateVisitorRequest request,
            BindingResult bindingResult,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "student/visitor-form";
        }
        try {
            visitorService.requestVisitorPass(request, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Visitor request submitted successfully!");
            return "redirect:/student/visitors";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "student/visitor-form";
        }
    }

    // ==========================================
    // 8. NOTICES
    // ==========================================
    @GetMapping("/notices")
    public String viewNotices(Model model) {
        model.addAttribute("notices", noticeRepository.findByActiveTrueOrderByCreatedAtDesc());
        return "student/notices";
    }

    // ==========================================
    // 9. MESS MENU
    // ==========================================
    @GetMapping("/mess")
    public String viewMess(Model model) {
        WeeklyMenuResponse weekly = messMenuService.getWeeklyMenu();
        model.addAttribute("weekly", weekly);
        return "student/mess";
    }
}
