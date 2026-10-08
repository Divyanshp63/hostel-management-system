package com.hostel.management.controller.web;

import com.hostel.management.dto.request.CreateFeeRequest;
import com.hostel.management.dto.request.PaymentRequest;
import com.hostel.management.dto.request.UpdateFeeRequest;
import com.hostel.management.dto.response.FeeResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.PaymentResponse;
import com.hostel.management.entity.Fee;
import com.hostel.management.entity.Payment;
import com.hostel.management.enums.FeeStatus;
import com.hostel.management.enums.PaymentMethod;
import com.hostel.management.repository.FeeRepository;
import com.hostel.management.repository.PaymentRepository;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.service.FeeService;
import com.hostel.management.service.PaymentService;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/accountant")
@RequiredArgsConstructor
@Slf4j
public class AccountantWebController {

    private final FeeService feeService;
    private final FeeRepository feeRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;

    // ==========================================
    // 1. ACCOUNTANT DASHBOARD
    // ==========================================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        BigDecimal totalBilled = feeRepository.sumTotalBilled();
        BigDecimal totalPaid = paymentRepository.sumSuccessfulPayments();
        BigDecimal totalRemaining = feeRepository.sumTotalRemaining();
        long overdueCount = feeRepository.countByStatus(FeeStatus.OVERDUE);
        long pendingCount = feeRepository.countByStatus(FeeStatus.PENDING);
        long partialCount = feeRepository.countByStatus(FeeStatus.PARTIAL);
        long paidCount = feeRepository.countByStatus(FeeStatus.PAID);

        model.addAttribute("totalBilled", totalBilled != null ? totalBilled : BigDecimal.ZERO);
        model.addAttribute("totalPaid", totalPaid != null ? totalPaid : BigDecimal.ZERO);
        model.addAttribute("totalRemaining", totalRemaining != null ? totalRemaining : BigDecimal.ZERO);
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("partialCount", partialCount);
        model.addAttribute("paidCount", paidCount);

        model.addAttribute("recentPayments", paymentRepository.findAll().stream()
                .sorted((a, b) -> b.getPaymentDate().compareTo(a.getPaymentDate()))
                .limit(8)
                .toList());

        return "accountant/dashboard";
    }

    // ==========================================
    // 2. FEE MANAGEMENT
    // ==========================================
    @GetMapping("/fees")
    public String listFees(
            @RequestParam(value = "status", required = false) FeeStatus status,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "search", required = false) String search,
            Model model
    ) {
        PageResponse<FeeResponse> page = feeService.getAllFees(0, 100, "id", "desc", search, status, month);
        model.addAttribute("fees", page.getContent());
        model.addAttribute("statuses", FeeStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("search", search);
        return "accountant/fees";
    }

    @GetMapping("/fees/new")
    public String showCreateFeeForm(Model model) {
        if (!model.containsAttribute("fee")) {
            String currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            CreateFeeRequest req = CreateFeeRequest.builder()
                    .month(currentMonth)
                    .roomRent(BigDecimal.valueOf(6000))
                    .messFee(BigDecimal.valueOf(2500))
                    .electricityFee(BigDecimal.valueOf(500))
                    .maintenanceFee(BigDecimal.valueOf(500))
                    .dueDate(LocalDate.now().plusDays(10))
                    .build();
            model.addAttribute("fee", req);
        }
        model.addAttribute("students", studentRepository.findAll());
        return "accountant/fee-form";
    }

    @PostMapping("/fees/save")
    public String createFee(
            @Valid @ModelAttribute("fee") CreateFeeRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("students", studentRepository.findAll());
            return "accountant/fee-form";
        }
        try {
            feeService.createFee(request);
            redirectAttributes.addFlashAttribute("successMessage", "Fee invoice generated successfully!");
            return "redirect:/accountant/fees";
        } catch (Exception ex) {
            model.addAttribute("students", studentRepository.findAll());
            model.addAttribute("errorMessage", ex.getMessage());
            return "accountant/fee-form";
        }
    }

    @GetMapping("/fees/edit/{id}")
    public String showEditFeeForm(@PathVariable Long id, Authentication auth, Model model) {
        Fee fee = feeRepository.findById(id).orElseThrow();
        UpdateFeeRequest req = UpdateFeeRequest.builder()
                .roomRent(fee.getRoomRent())
                .messFee(fee.getMessFee())
                .electricityFee(fee.getElectricityFee())
                .maintenanceFee(fee.getMaintenanceFee())
                .dueDate(fee.getDueDate())
                .remarks(fee.getRemarks())
                .build();

        model.addAttribute("fee", req);
        model.addAttribute("feeId", id);
        model.addAttribute("studentName", fee.getStudent().getUser().getName());
        model.addAttribute("admissionNumber", fee.getStudent().getAdmissionNumber());
        model.addAttribute("month", fee.getMonth());
        model.addAttribute("paidAmount", fee.getPaidAmount());
        return "accountant/fee-edit";
    }

    @PostMapping("/fees/update/{id}")
    public String updateFee(
            @PathVariable Long id,
            @Valid @ModelAttribute("fee") UpdateFeeRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Fee fee = feeRepository.findById(id).orElseThrow();
        if (bindingResult.hasErrors()) {
            model.addAttribute("feeId", id);
            model.addAttribute("studentName", fee.getStudent().getUser().getName());
            model.addAttribute("admissionNumber", fee.getStudent().getAdmissionNumber());
            model.addAttribute("month", fee.getMonth());
            model.addAttribute("paidAmount", fee.getPaidAmount());
            return "accountant/fee-edit";
        }
        try {
            feeService.updateFee(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Fee invoice updated successfully!");
            return "redirect:/accountant/fees";
        } catch (Exception ex) {
            model.addAttribute("feeId", id);
            model.addAttribute("studentName", fee.getStudent().getUser().getName());
            model.addAttribute("admissionNumber", fee.getStudent().getAdmissionNumber());
            model.addAttribute("month", fee.getMonth());
            model.addAttribute("paidAmount", fee.getPaidAmount());
            model.addAttribute("errorMessage", ex.getMessage());
            return "accountant/fee-edit";
        }
    }

    @PostMapping("/fees/delete/{id}")
    public String deleteFee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            feeService.deleteFee(id);
            redirectAttributes.addFlashAttribute("successMessage", "Fee invoice removed.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/accountant/fees";
    }

    // ==========================================
    // 3. PENDING FEES
    // ==========================================
    @GetMapping("/fees/pending")
    public String listPendingFees(Model model) {
        List<Fee> pendingList = feeRepository.findAll().stream()
                .filter(f -> f.getStatus() != FeeStatus.PAID)
                .sorted((a, b) -> a.getDueDate().compareTo(b.getDueDate()))
                .toList();

        BigDecimal totalPending = pendingList.stream()
                .map(Fee::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("fees", pendingList);
        model.addAttribute("totalPending", totalPending);
        return "accountant/pending-fees";
    }

    // ==========================================
    // 4. PAYMENTS & RECORDING
    // ==========================================
    @GetMapping("/payments")
    public String listPayments(Model model) {
        List<Payment> payments = paymentRepository.findAll().stream()
                .sorted((a, b) -> b.getPaymentDate().compareTo(a.getPaymentDate()))
                .toList();
        model.addAttribute("payments", payments);
        return "accountant/payments";
    }

    @GetMapping("/payments/record")
    public String showRecordPaymentForm(@RequestParam(value = "feeId", required = false) Long feeId, Model model) {
        List<Fee> pendingFees = feeRepository.findAll().stream()
                .filter(f -> f.getStatus() != FeeStatus.PAID)
                .toList();

        PaymentRequest req = new PaymentRequest();
        if (feeId != null) {
            feeRepository.findById(feeId).ifPresent(f -> {
                req.setFeeId(f.getId());
                req.setAmount(f.getRemainingAmount());
            });
        }

        model.addAttribute("payment", req);
        model.addAttribute("pendingFees", pendingFees);
        model.addAttribute("methods", PaymentMethod.values());
        return "accountant/payment-record";
    }

    @PostMapping("/payments/record")
    public String recordPayment(
            @Valid @ModelAttribute("payment") PaymentRequest request,
            BindingResult bindingResult,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pendingFees", feeRepository.findAll().stream().filter(f -> f.getStatus() != FeeStatus.PAID).toList());
            model.addAttribute("methods", PaymentMethod.values());
            return "accountant/payment-record";
        }
        try {
            PaymentResponse res = paymentService.processPayment(request, auth.getName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment of ₹" + request.getAmount() + " recorded successfully! Txn ID: " + res.getTransactionId());
            return "redirect:/accountant/payments";
        } catch (Exception ex) {
            model.addAttribute("pendingFees", feeRepository.findAll().stream().filter(f -> f.getStatus() != FeeStatus.PAID).toList());
            model.addAttribute("methods", PaymentMethod.values());
            model.addAttribute("errorMessage", ex.getMessage());
            return "accountant/payment-record";
        }
    }

    // ==========================================
    // 5. REPORTS
    // ==========================================
    @GetMapping("/reports")
    public String viewReports(Model model) {
        BigDecimal totalBilled = feeRepository.sumTotalBilled();
        BigDecimal totalCollected = paymentRepository.sumSuccessfulPayments();
        BigDecimal totalPending = feeRepository.sumTotalRemaining();
        long totalInvoices = feeRepository.count();
        long paidInvoices = feeRepository.countByStatus(FeeStatus.PAID);
        long pendingInvoices = feeRepository.countByStatus(FeeStatus.PENDING);
        long partialInvoices = feeRepository.countByStatus(FeeStatus.PARTIAL);
        long overdueInvoices = feeRepository.countByStatus(FeeStatus.OVERDUE);

        model.addAttribute("totalBilled", totalBilled != null ? totalBilled : BigDecimal.ZERO);
        model.addAttribute("totalCollected", totalCollected != null ? totalCollected : BigDecimal.ZERO);
        model.addAttribute("totalPending", totalPending != null ? totalPending : BigDecimal.ZERO);
        model.addAttribute("totalInvoices", totalInvoices);
        model.addAttribute("paidInvoices", paidInvoices);
        model.addAttribute("pendingInvoices", pendingInvoices);
        model.addAttribute("partialInvoices", partialInvoices);
        model.addAttribute("overdueInvoices", overdueInvoices);

        model.addAttribute("payments", paymentRepository.findAll());
        model.addAttribute("fees", feeRepository.findAll());
        return "accountant/reports";
    }
}
