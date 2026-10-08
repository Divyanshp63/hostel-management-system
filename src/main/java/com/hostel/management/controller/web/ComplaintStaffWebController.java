package com.hostel.management.controller.web;

import com.hostel.management.dto.response.ComplaintResponse;
import com.hostel.management.entity.Staff;
import com.hostel.management.entity.User;
import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.ComplaintPriority;
import com.hostel.management.enums.ComplaintStatus;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.ComplaintRepository;
import com.hostel.management.repository.StaffRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping({"/complaint-staff", "/maintenance"})
@RequiredArgsConstructor
@Slf4j
public class ComplaintStaffWebController {

    private final ComplaintService complaintService;
    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String email = auth.getName();
        User staffUser = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found: " + email));

        Staff staff = staffRepository.findByUser(staffUser).orElse(null);
        ComplaintCategory department = staff != null && staff.getDepartment() != null
                ? staff.getDepartment() : ComplaintCategory.MAINTENANCE;

        // Dynamic MySQL-backed Counters as required in Section 16:
        // New Complaints, In Progress, Resolved, Returned, Total Active Complaints
        long newComplaintsCount = complaintRepository.countByStatus(ComplaintStatus.SUBMITTED);
        long inProgressCount = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolvedCount = complaintRepository.countByStatus(ComplaintStatus.RESOLVED_BY_MAINTENANCE);
        long returnedCount = complaintRepository.countByStatus(ComplaintStatus.RETURNED);
        long totalActiveComplaints = newComplaintsCount + inProgressCount + returnedCount;

        long highPriorityCount = complaintRepository.countForStaffMemberByPriority(staffUser.getId(), department, ComplaintPriority.HIGH);
        long criticalPriorityCount = complaintRepository.countForStaffMemberByPriority(staffUser.getId(), department, ComplaintPriority.CRITICAL);
        long slaBreachedCount = complaintRepository.countSlaBreachedTotal(LocalDateTime.now());

        List<ComplaintResponse> complaints = complaintService.getComplaintsForStaff(email);

        model.addAttribute("staff", staff);
        model.addAttribute("staffUser", staffUser);
        model.addAttribute("departmentName", "Maintenance Department");

        model.addAttribute("newComplaintsCount", newComplaintsCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("returnedCount", returnedCount);
        model.addAttribute("totalActiveComplaints", totalActiveComplaints);

        model.addAttribute("highPriorityCount", highPriorityCount);
        model.addAttribute("criticalPriorityCount", criticalPriorityCount);
        model.addAttribute("slaBreachedCount", slaBreachedCount);
        model.addAttribute("complaints", complaints);

        return "complaint-staff/dashboard";
    }

    @GetMapping("/complaints")
    public String listComplaints(Authentication auth, Model model) {
        String email = auth.getName();
        User staffUser = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
        Staff staff = staffUser != null ? staffRepository.findByUser(staffUser).orElse(null) : null;

        List<ComplaintResponse> complaints = complaintService.getComplaintsForStaff(email);

        long newComplaintsCount = complaintRepository.countByStatus(ComplaintStatus.SUBMITTED);
        long inProgressCount = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolvedCount = complaintRepository.countByStatus(ComplaintStatus.RESOLVED_BY_MAINTENANCE);
        long returnedCount = complaintRepository.countByStatus(ComplaintStatus.RETURNED);
        long totalActiveComplaints = newComplaintsCount + inProgressCount + returnedCount;

        model.addAttribute("complaints", complaints);
        model.addAttribute("staffUser", staffUser);
        model.addAttribute("staff", staff);
        model.addAttribute("newComplaintsCount", newComplaintsCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("returnedCount", returnedCount);
        model.addAttribute("totalActiveComplaints", totalActiveComplaints);

        return "complaint-staff/complaints";
    }

    @PostMapping("/complaints/accept/{id}")
    public String acceptComplaint(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes, @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            complaintService.acceptComplaint(id, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Complaint ticket accepted! Status updated to IN_PROGRESS.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return referer != null && !referer.isBlank() ? "redirect:" + referer : "redirect:/complaint-staff/dashboard";
    }

    @PostMapping("/complaints/resolve/{id}")
    public String resolveComplaint(
            @PathVariable Long id,
            @RequestParam("resolutionRemarks") String remarks,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer
    ) {
        try {
            complaintService.resolveComplaint(id, remarks, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Complaint marked as RESOLVED_BY_MAINTENANCE and sent to Warden for verification!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return referer != null && !referer.isBlank() ? "redirect:" + referer : "redirect:/complaint-staff/dashboard";
    }

    @PostMapping("/complaints/notes/{id}")
    public String addWorkNotes(
            @PathVariable Long id,
            @RequestParam("notes") String notes,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            @RequestHeader(value = "Referer", required = false) String referer
    ) {
        try {
            complaintService.addWorkNotes(id, notes, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Work note added successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return referer != null && !referer.isBlank() ? "redirect:" + referer : "redirect:/complaint-staff/dashboard";
    }
}
