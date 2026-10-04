package com.hostel.management.service;

public interface PdfReportService {

    byte[] generateFeeReceiptPdf(Long paymentId, String currentUserEmail, boolean isAdminOrAccountant);

    byte[] generateMonthlyFinancialReportPdf(String month);

    byte[] generateOccupancyReportPdf();
}
