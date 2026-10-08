package com.hostel.management.controller.web;

import com.hostel.management.dto.request.*;
import com.hostel.management.dto.response.*;
import com.hostel.management.entity.*;
import com.hostel.management.enums.*;
import com.hostel.management.repository.*;
import com.hostel.management.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping({"/warden", "/admin"})
@RequiredArgsConstructor
@Slf4j
public class WardenWebController {

    private final DashboardService dashboardService;
    private final StudentService studentService;
    private final RoomService roomService;
    private final RoomAllocationService roomAllocationService;
    private final ComplaintService complaintService;
    private final LeaveService leaveService;
    private final VisitorService visitorService;
    private final NoticeService noticeService;
    private final MessMenuService messMenuService;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final RoomAllocationRepository roomAllocationRepository;
    private final ComplaintRepository complaintRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final VisitorRequestRepository visitorRequestRepository;
    private final NoticeRepository noticeRepository;
    private final MessMenuRepository messMenuRepository;
    private final UserRepository userRepository;
    private final FeeRepository feeRepository;
    private final PaymentRepository paymentRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    // ==========================================
    // 1. WARDEN DASHBOARD
    // ==========================================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        AdminDashboardResponse stats = dashboardService.getAdminDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("recentComplaints", complaintRepository.findTop5ByOrderByCreatedAtDesc());
        model.addAttribute("pendingAllocations", roomAllocationRepository.findTop5ByStatusOrderByCreatedAtDesc(AllocationStatus.PENDING));
        model.addAttribute("pendingLeavesCount", leaveRequestRepository.countByStatus(LeaveStatus.PENDING));
        model.addAttribute("pendingVisitorsCount", visitorRequestRepository.countByStatus(VisitorStatus.PENDING));
        model.addAttribute("escalatedComplaintsCount", complaintService.countSlaBreached());
        return "warden/dashboard";
    }

    // ==========================================
    // 2. STUDENT MANAGEMENT (CRUD)
    // ==========================================
    @GetMapping("/students")
    public String listStudents(@RequestParam(value = "search", required = false) String search, Model model) {
        PageResponse<StudentResponse> page = studentService.getAllStudents(0, 100, "id", "desc", search);
        model.addAttribute("students", page.getContent());
        model.addAttribute("search", search);
        return "warden/students";
    }

    @GetMapping("/students/new")
    public String showCreateStudentForm(Model model) {
        if (!model.containsAttribute("student")) {
            model.addAttribute("student", new CreateStudentRequest());
        }
        model.addAttribute("genders", Gender.values());
        return "warden/student-form";
    }

    @PostMapping("/students/save")
    public String createStudent(
            @Valid @ModelAttribute("student") CreateStudentRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("genders", Gender.values());
            return "warden/student-form";
        }
        try {
            studentService.createStudent(request);
            redirectAttributes.addFlashAttribute("successMessage", "Student added successfully!");
            return "redirect:/warden/students";
        } catch (Exception ex) {
            model.addAttribute("genders", Gender.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "warden/student-form";
        }
    }

    @GetMapping("/students/edit/{id}")
    public String showEditStudentForm(@PathVariable Long id, Model model) {
        Student student = studentRepository.findById(id).orElseThrow();
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
        model.addAttribute("studentId", id);
        model.addAttribute("email", student.getUser().getEmail());
        model.addAttribute("admissionNumber", student.getAdmissionNumber());
        model.addAttribute("genders", Gender.values());
        return "warden/student-edit";
    }

    @PostMapping("/students/update/{id}")
    public String updateStudent(
            @PathVariable Long id,
            @Valid @ModelAttribute("student") UpdateStudentRequest request,
            BindingResult bindingResult,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("studentId", id);
            model.addAttribute("genders", Gender.values());
            return "warden/student-edit";
        }
        try {
            studentService.updateStudent(id, request, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Student updated successfully!");
            return "redirect:/warden/students";
        } catch (Exception ex) {
            model.addAttribute("studentId", id);
            model.addAttribute("genders", Gender.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "warden/student-edit";
        }
    }

    @GetMapping("/students/view/{id}")
    public String viewStudentProfile(@PathVariable Long id, Model model) {
        Student student = studentRepository.findById(id).orElseThrow();
        RoomAllocation activeAllocation = roomAllocationRepository.findByStudentIdAndStatus(id, AllocationStatus.ACTIVE).orElse(null);
        List<RoomAllocation> allocationHistory = roomAllocationRepository.findByStudentIdOrderByCreatedAtDesc(id);

        model.addAttribute("student", student);
        model.addAttribute("activeAllocation", activeAllocation);
        model.addAttribute("allocationHistory", allocationHistory);
        model.addAttribute("fees", feeRepository.findByStudentIdOrderByDueDateDesc(id));
        model.addAttribute("complaints", complaintRepository.findByStudentIdOrderByCreatedAtDesc(id));
        model.addAttribute("leaves", leaveRequestRepository.findByStudentIdOrderByCreatedAtDesc(id));
        model.addAttribute("visitors", visitorRequestRepository.findByStudentIdOrderByVisitDateDesc(id));
        return "warden/student-view";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student record deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/students";
    }

    // ==========================================
    // 3. ROOM MANAGEMENT (CRUD)
    // ==========================================
    @GetMapping("/rooms")
    public String listRooms(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) RoomStatus status,
            Model model
    ) {
        PageResponse<RoomResponse> page = roomService.getAllRooms(0, 100, "roomNumber", "asc", search, status, null);
        model.addAttribute("rooms", page.getContent());
        model.addAttribute("statuses", RoomStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        return "warden/rooms";
    }

    @GetMapping("/rooms/new")
    public String showCreateRoomForm(Model model) {
        if (!model.containsAttribute("room")) {
            model.addAttribute("room", new CreateRoomRequest());
        }
        model.addAttribute("roomTypes", RoomType.values());
        return "warden/room-form";
    }

    @PostMapping("/rooms/save")
    public String createRoom(
            @Valid @ModelAttribute("room") CreateRoomRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roomTypes", RoomType.values());
            return "warden/room-form";
        }
        try {
            roomService.createRoom(request);
            redirectAttributes.addFlashAttribute("successMessage", "Room created successfully!");
            return "redirect:/warden/rooms";
        } catch (Exception ex) {
            model.addAttribute("roomTypes", RoomType.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "warden/room-form";
        }
    }

    @GetMapping("/rooms/edit/{id}")
    public String showEditRoomForm(@PathVariable Long id, Model model) {
        Room room = roomRepository.findById(id).orElseThrow();
        UpdateRoomRequest req = UpdateRoomRequest.builder()
                .floor(room.getFloor())
                .roomType(room.getRoomType())
                .capacity(room.getCapacity())
                .rentPerMonth(room.getRentPerMonth())
                .status(room.getStatus())
                .description(room.getDescription())
                .build();

        model.addAttribute("room", req);
        model.addAttribute("roomId", id);
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("roomStatuses", RoomStatus.values());
        return "warden/room-edit";
    }

    @PostMapping("/rooms/update/{id}")
    public String updateRoom(
            @PathVariable Long id,
            @Valid @ModelAttribute("room") UpdateRoomRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roomId", id);
            model.addAttribute("roomTypes", RoomType.values());
            model.addAttribute("roomStatuses", RoomStatus.values());
            return "warden/room-edit";
        }
        try {
            roomService.updateRoom(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Room details updated successfully!");
            return "redirect:/warden/rooms";
        } catch (Exception ex) {
            model.addAttribute("roomId", id);
            model.addAttribute("roomTypes", RoomType.values());
            model.addAttribute("roomStatuses", RoomStatus.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "warden/room-edit";
        }
    }

    @PostMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/rooms";
    }

    // ==========================================
    // 4. ROOM ALLOCATIONS
    // ==========================================
    @GetMapping("/allocations")
    public String listAllocations(
            @RequestParam(value = "status", required = false) AllocationStatus status,
            @RequestParam(value = "search", required = false) String search,
            Model model
    ) {
        PageResponse<RoomAllocationResponse> page = roomAllocationService.getAllAllocations(0, 100, "id", "desc", search, status);
        model.addAttribute("allocations", page.getContent());
        model.addAttribute("statuses", AllocationStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        return "warden/allocations";
    }

    @GetMapping("/allocations/new")
    public String showDirectAllocationForm(Model model) {
        model.addAttribute("allocation", new AdminAllocationRequest());
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("availableRooms", roomRepository.findByStatus(RoomStatus.AVAILABLE));
        return "warden/allocation-form";
    }

    @PostMapping("/allocations/save")
    public String directAllocateRoom(
            @Valid @ModelAttribute("allocation") AdminAllocationRequest request,
            BindingResult bindingResult,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("students", studentRepository.findAll());
            model.addAttribute("availableRooms", roomRepository.findByStatus(RoomStatus.AVAILABLE));
            return "warden/allocation-form";
        }
        try {
            roomAllocationService.directAllocate(request);
            redirectAttributes.addFlashAttribute("successMessage", "Room allocated successfully!");
            return "redirect:/warden/allocations";
        } catch (Exception ex) {
            model.addAttribute("students", studentRepository.findAll());
            model.addAttribute("availableRooms", roomRepository.findByStatus(RoomStatus.AVAILABLE));
            model.addAttribute("errorMessage", ex.getMessage());
            return "warden/allocation-form";
        }
    }

    @PostMapping("/allocations/approve/{id}")
    public String approveAllocation(
            @PathVariable Long id,
            @RequestParam(value = "remarks", required = false) String remarks,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ApprovalRequest req = ApprovalRequest.builder().remarks(remarks).build();
            roomAllocationService.approveAllocation(id, req);
            redirectAttributes.addFlashAttribute("successMessage", "Room allocation approved successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/allocations";
    }

    @PostMapping("/allocations/reject/{id}")
    public String rejectAllocation(
            @PathVariable Long id,
            @RequestParam("reason") String reason,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            RejectionRequest req = RejectionRequest.builder().reason(reason).build();
            roomAllocationService.rejectAllocation(id, req);
            redirectAttributes.addFlashAttribute("successMessage", "Room allocation request rejected.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/allocations";
    }

    @PostMapping("/allocations/vacate/{id}")
    public String vacateRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomAllocationService.vacateAllocation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student successfully checked-out. Bed freed!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/allocations";
    }

    // ==========================================
    // 5. COMPLAINT MONITORING & VERIFICATION WORKFLOW
    // ==========================================
    @GetMapping("/complaints")
    public String listComplaints(
            @RequestParam(value = "status", required = false) ComplaintStatus status,
            @RequestParam(value = "category", required = false) ComplaintCategory category,
            @RequestParam(value = "search", required = false) String search,
            Model model
    ) {
        // 1. Complaint Verification Queue: Complaints resolved by Maintenance waiting for Warden verification
        List<ComplaintResponse> pendingVerification = complaintService.getComplaintsPendingVerification();
        model.addAttribute("pendingVerification", pendingVerification);

        // 2. Full Complaints Monitoring Log
        PageResponse<ComplaintResponse> page = complaintService.getAllComplaints(0, 100, "id", "desc", search, status, category);
        model.addAttribute("complaints", page.getContent());

        // 3. Dynamic MySQL-backed Counters
        long pendingVerificationCount = complaintRepository.countByStatus(ComplaintStatus.RESOLVED_BY_MAINTENANCE);
        long verifiedCount = complaintRepository.countByStatusIn(List.of(ComplaintStatus.VERIFIED, ComplaintStatus.CLOSED));
        long returnedCount = complaintRepository.countByStatus(ComplaintStatus.RETURNED);
        long totalComplaintsCount = complaintRepository.count();

        model.addAttribute("pendingVerificationCount", pendingVerificationCount);
        model.addAttribute("verifiedCount", verifiedCount);
        model.addAttribute("returnedCount", returnedCount);
        model.addAttribute("totalComplaintsCount", totalComplaintsCount);

        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("categories", ComplaintCategory.values());
        model.addAttribute("priorities", ComplaintPriority.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("search", search);
        model.addAttribute("slaBreachedCount", complaintService.countSlaBreached());
        return "warden/complaints";
    }

    @PostMapping("/complaints/verify/{id}")
    public String verifyComplaint(
            @PathVariable Long id,
            @RequestParam(value = "verificationRemarks", required = false) String verificationRemarks,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            complaintService.verifyComplaintByWarden(id, verificationRemarks, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Complaint #" + id + " verified successfully and marked as CLOSED.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/complaints";
    }

    @PostMapping("/complaints/return/{id}")
    public String returnComplaint(
            @PathVariable Long id,
            @RequestParam(value = "reason", required = false) String reason,
            @RequestParam(value = "rejectionReason", required = false) String rejectionReason,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String effectiveReason = reason != null && !reason.isBlank() ? reason : rejectionReason;
            if (effectiveReason == null || effectiveReason.isBlank()) {
                effectiveReason = "Work not completed satisfactorily. Returned for reinspection.";
            }
            complaintService.returnComplaintByWarden(id, effectiveReason, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Complaint #" + id + " returned to Maintenance Department for rework.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/complaints";
    }

    // ==========================================
    // 6. LEAVE REQUEST MANAGEMENT (END-TO-END)
    // ==========================================
    @GetMapping("/leaves")
    public String listLeaves(
            @RequestParam(value = "status", required = false) LeaveStatus status,
            @RequestParam(value = "search", required = false) String search,
            Authentication auth,
            Model model
    ) {
        if (auth == null) {
            return "redirect:/login";
        }
        PageResponse<LeaveResponse> page = leaveService.getAllLeaves(0, 100, "id", "desc", search, status, null);
        model.addAttribute("leaves", page != null && page.getContent() != null ? page.getContent() : java.util.Collections.emptyList());
        model.addAttribute("statuses", LeaveStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        return "warden/leaves";
    }

    @PostMapping("/leaves/approve/{id}")
    public String approveLeave(
            @PathVariable Long id,
            @RequestParam(value = "adminRemarks", required = false) String remarks,
            @RequestParam(value = "WardenRemarks", required = false) String wardenRemarks,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        if (auth == null) {
            return "redirect:/login";
        }
        try {
            String finalRemarks = (remarks != null && !remarks.isBlank()) ? remarks : wardenRemarks;
            LeaveActionRequest req = LeaveActionRequest.builder().remarks(finalRemarks).build();
            leaveService.approveLeave(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Leave request APPROVED successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/leaves";
    }

    @PostMapping("/leaves/reject/{id}")
    public String rejectLeave(
            @PathVariable Long id,
            @RequestParam(value = "adminRemarks", required = false) String remarks,
            @RequestParam(value = "WardenRemarks", required = false) String wardenRemarks,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        if (auth == null) {
            return "redirect:/login";
        }
        try {
            String finalRemarks = (remarks != null && !remarks.isBlank()) ? remarks : wardenRemarks;
            LeaveActionRequest req = LeaveActionRequest.builder().remarks(finalRemarks).build();
            leaveService.rejectLeave(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Leave request REJECTED.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/leaves";
    }

    // ==========================================
    // 7. VISITOR REQUEST MANAGEMENT
    // ==========================================
    @GetMapping("/visitors")
    public String listVisitors(
            @RequestParam(value = "status", required = false) VisitorStatus status,
            @RequestParam(value = "search", required = false) String search,
            Model model
    ) {
        PageResponse<VisitorResponse> page = visitorService.getAllVisitorRequests(0, 100, "id", "desc", search, status, null);
        model.addAttribute("visitors", page.getContent());
        model.addAttribute("statuses", VisitorStatus.values());
        model.addAttribute("selectedStatus", status);
        return "warden/visitors";
    }

    @PostMapping("/visitors/approve/{id}")
    public String approveVisitor(
            @PathVariable Long id,
            @RequestParam(value = "adminRemarks", required = false) String remarks,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            VisitorApprovalRequest req = VisitorApprovalRequest.builder()
                    .adminRemarks(remarks)
                    .checkInTime(LocalDateTime.now())
                    .build();
            visitorService.approveVisitorRequest(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Visitor request approved.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/visitors";
    }

    @PostMapping("/visitors/reject/{id}")
    public String rejectVisitor(
            @PathVariable Long id,
            @RequestParam(value = "adminRemarks", required = false) String remarks,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            VisitorRejectionRequest req = VisitorRejectionRequest.builder()
                    .reason(remarks != null && !remarks.isBlank() ? remarks : "Rejected by Warden")
                    .build();
            visitorService.rejectVisitorRequest(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Visitor request rejected.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/visitors";
    }

    @PostMapping("/visitors/checkin/{id}")
    public String checkinVisitor(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            VisitorApprovalRequest req = VisitorApprovalRequest.builder()
                    .adminRemarks("Checked in")
                    .checkInTime(LocalDateTime.now())
                    .build();
            visitorService.approveVisitorRequest(id, req, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Visitor checked in successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/visitors";
    }

    @PostMapping("/visitors/checkout/{id}")
    public String checkoutVisitor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            visitorService.completeVisit(id);
            redirectAttributes.addFlashAttribute("successMessage", "Visitor checked out successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/visitors";
    }

    // ==========================================
    // 8. NOTICE MANAGEMENT (CRUD)
    // ==========================================
    @GetMapping("/notices")
    public String listNotices(Model model) {
        model.addAttribute("notices", noticeRepository.findAll());
        return "warden/notices";
    }

    @GetMapping("/notices/new")
    public String showCreateNoticeForm(Model model) {
        if (!model.containsAttribute("notice")) {
            model.addAttribute("notice", new CreateNoticeRequest());
        }
        model.addAttribute("priorities", NoticePriority.values());
        model.addAttribute("targets", NoticeTarget.values());
        return "warden/notice-form";
    }

    @PostMapping("/notices/save")
    public String saveNotice(
            @Valid @ModelAttribute("notice") CreateNoticeRequest request,
            BindingResult bindingResult,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("priorities", NoticePriority.values());
            model.addAttribute("targets", NoticeTarget.values());
            return "warden/notice-form";
        }
        try {
            noticeService.createNotice(request, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Notice published successfully!");
            return "redirect:/warden/notices";
        } catch (Exception ex) {
            model.addAttribute("priorities", NoticePriority.values());
            model.addAttribute("targets", NoticeTarget.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "warden/notice-form";
        }
    }

    @GetMapping("/notices/edit/{id}")
    public String showEditNoticeForm(@PathVariable Long id, Model model) {
        Notice notice = noticeRepository.findById(id).orElseThrow();
        UpdateNoticeRequest req = UpdateNoticeRequest.builder()
                .title(notice.getTitle())
                .content(notice.getContent())
                .priority(notice.getPriority())
                .targetAudience(notice.getTargetAudience())
                .expiryDate(notice.getExpiryDate())
                .active(notice.isActive())
                .build();

        model.addAttribute("notice", req);
        model.addAttribute("noticeId", id);
        model.addAttribute("priorities", NoticePriority.values());
        model.addAttribute("targets", NoticeTarget.values());
        return "warden/notice-edit";
    }

    @PostMapping("/notices/update/{id}")
    public String updateNotice(
            @PathVariable Long id,
            @Valid @ModelAttribute("notice") UpdateNoticeRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("noticeId", id);
            model.addAttribute("priorities", NoticePriority.values());
            model.addAttribute("targets", NoticeTarget.values());
            return "warden/notice-edit";
        }
        try {
            noticeService.updateNotice(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Notice updated successfully!");
            return "redirect:/warden/notices";
        } catch (Exception ex) {
            model.addAttribute("noticeId", id);
            model.addAttribute("priorities", NoticePriority.values());
            model.addAttribute("targets", NoticeTarget.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "warden/notice-edit";
        }
    }

    @PostMapping("/notices/delete/{id}")
    public String deleteNotice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            noticeService.deleteNotice(id);
            redirectAttributes.addFlashAttribute("successMessage", "Notice deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/notices";
    }

    // ==========================================
    // 9. MESS MENU MANAGEMENT (CRUD)
    // ==========================================
    @GetMapping("/mess")
    public String viewMessMenu(Model model) {
        WeeklyMenuResponse weekly = messMenuService.getWeeklyMenu();
        model.addAttribute("weekly", weekly);
        return "warden/mess";
    }

    @GetMapping("/mess/edit/{id}")
    public String showEditMessMenu(@PathVariable Long id, Model model) {
        MessMenu menu = messMenuRepository.findById(id).orElseThrow();
        MessMenuRequest req = MessMenuRequest.builder()
                .dayOfWeek(menu.getDayOfWeek())
                .mealType(menu.getMealType())
                .items(menu.getItems())
                .timing(menu.getTiming())
                .specialDiet(menu.getSpecialDiet())
                .build();
        model.addAttribute("menu", req);
        model.addAttribute("menuId", id);
        return "warden/mess-edit";
    }

    @PostMapping("/mess/update/{id}")
    public String updateMessMenu(
            @PathVariable Long id,
            @Valid @ModelAttribute("menu") MessMenuRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("menuId", id);
            return "warden/mess-edit";
        }
        try {
            messMenuService.createOrUpdateMenu(request);
            redirectAttributes.addFlashAttribute("successMessage", "Mess meal timetable updated successfully!");
            return "redirect:/warden/mess";
        } catch (Exception ex) {
            model.addAttribute("menuId", id);
            model.addAttribute("errorMessage", ex.getMessage());
            return "warden/mess-edit";
        }
    }

    // ==========================================
    // 10. REPORTS SECTION
    // ==========================================
    @GetMapping("/reports")
    public String viewReports(Model model) {
        long totalStudents = studentRepository.count();
        long totalRooms = roomRepository.count();
        Long totalOccupied = roomRepository.sumTotalOccupied();
        long occupiedBeds = totalOccupied != null ? totalOccupied : 0L;
        Long totalCap = roomRepository.sumTotalCapacity();
        long totalBeds = totalCap != null ? totalCap : 0L;
        long availableBeds = Math.max(0, totalBeds - occupiedBeds);
        BigDecimal pendingFees = feeRepository.sumTotalRemaining();
        BigDecimal feeCollection = paymentRepository.sumSuccessfulPayments();
        long pendingComplaints = complaintRepository.countByStatusIn(List.of(ComplaintStatus.SUBMITTED, ComplaintStatus.IN_PROGRESS, ComplaintStatus.RETURNED));
        long resolvedComplaints = complaintRepository.countByStatus(ComplaintStatus.RESOLVED_BY_MAINTENANCE) + complaintRepository.countByStatus(ComplaintStatus.VERIFIED) + complaintRepository.countByStatus(ComplaintStatus.CLOSED);
        long activeAllocations = roomAllocationRepository.countByStatus(AllocationStatus.ACTIVE);
        long escalatedComplaints = complaintService.countSlaBreached();

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("occupiedBeds", occupiedBeds);
        model.addAttribute("availableBeds", availableBeds);
        model.addAttribute("totalBeds", totalBeds);
        model.addAttribute("pendingFees", pendingFees != null ? pendingFees : BigDecimal.ZERO);
        model.addAttribute("feeCollection", feeCollection != null ? feeCollection : BigDecimal.ZERO);
        model.addAttribute("pendingComplaints", pendingComplaints);
        model.addAttribute("resolvedComplaints", resolvedComplaints);
        model.addAttribute("activeAllocations", activeAllocations);
        model.addAttribute("escalatedComplaints", escalatedComplaints);

        model.addAttribute("allocationsList", roomAllocationRepository.findAll());
        model.addAttribute("recentPayments", paymentRepository.findAll());
        model.addAttribute("complaintCategories", complaintRepository.countComplaintsByCategory());

        return "warden/reports";
    }

    // ==========================================
    // 11. USER MANAGEMENT (STAFF & WARDENS)
    // ==========================================
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("roles", Role.values());
        return "warden/users";
    }

    @PostMapping("/users/new")
    public String createUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("phone") String phone,
            @RequestParam("role") Role role,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (userRepository.existsByEmail(email.trim().toLowerCase())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email is already registered!");
                return "redirect:/warden/users";
            }
            User user = User.builder()
                    .name(name.trim())
                    .email(email.trim().toLowerCase())
                    .password(passwordEncoder.encode(password))
                    .phone(phone.trim())
                    .role(role)
                    .enabled(true)
                    .build();
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "New staff account created successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/users";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElseThrow();
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "User status changed successfully to: " + (user.isEnabled() ? "ACTIVE" : "DISABLED"));
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/warden/users";
    }
}
