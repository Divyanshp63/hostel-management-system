package com.hostel.management.controller;

import com.hostel.management.service.PdfReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final PdfReportService pdfReportService;

    @GetMapping("/fee-receipt/{paymentId}")
    @PreAuthorize("hasAnyRole('WARDEN', 'ACCOUNTANT', 'STUDENT')")
    public ResponseEntity<byte[]> downloadFeeReceipt(
            @PathVariable Long paymentId,
            Authentication authentication) {

        String currentUserEmail = authentication.getName();
        boolean isWardenOrAccountant = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_WARDEN") || role.equals("ROLE_ACCOUNTANT"));

        byte[] pdfBytes = pdfReportService.generateFeeReceiptPdf(paymentId, currentUserEmail, isWardenOrAccountant);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "Fee_Receipt_REC_" + paymentId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/financial/monthly")
    @PreAuthorize("hasAnyRole('WARDEN', 'ACCOUNTANT')")
    public ResponseEntity<byte[]> downloadMonthlyFinancialReport(
            @RequestParam String month) {

        byte[] pdfBytes = pdfReportService.generateMonthlyFinancialReportPdf(month);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Hostel_Financial_Report_" + month + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/occupancy")
    @PreAuthorize("hasAnyRole('WARDEN', 'ACCOUNTANT')")
    public ResponseEntity<byte[]> downloadOccupancyReport() {

        byte[] pdfBytes = pdfReportService.generateOccupancyReportPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Hostel_Occupancy_Roster.pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
