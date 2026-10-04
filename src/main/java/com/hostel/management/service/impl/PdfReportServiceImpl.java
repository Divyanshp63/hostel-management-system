package com.hostel.management.service.impl;

import com.hostel.management.entity.Fee;
import com.hostel.management.entity.Payment;
import com.hostel.management.entity.Room;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.exception.UnauthorizedException;
import com.hostel.management.repository.FeeRepository;
import com.hostel.management.repository.PaymentRepository;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.service.PdfReportService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PdfReportServiceImpl implements PdfReportService {

    private final PaymentRepository paymentRepository;
    private final FeeRepository feeRepository;
    private final RoomRepository roomRepository;

    private static final Color PRIMARY_COLOR = new Color(79, 70, 229);    // #4F46E5 Indigo
    private static final Color HEADER_BG = new Color(243, 244, 246);       // Light Gray
    private static final Color ACCENT_SUCCESS = new Color(16, 185, 129);   // #10B981 Green
    private static final Color TEXT_DARK = new Color(15, 23, 42);          // Slate Dark
    private static final Color TEXT_MUTED = new Color(100, 116, 139);      // Slate Muted

    @Override
    public byte[] generateFeeReceiptPdf(Long paymentId, String currentUserEmail, boolean isAdminOrAccountant) {
        log.info("Generating PDF receipt for payment ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        // Ownership enforcement for student users
        if (!isAdminOrAccountant) {
            String studentUserEmail = payment.getStudent().getUser().getEmail();
            if (!studentUserEmail.equalsIgnoreCase(currentUserEmail)) {
                throw new UnauthorizedException("You are not authorized to download this receipt.");
            }
        }

        Fee fee = payment.getFee();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Title Banner
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, PRIMARY_COLOR);
            Paragraph title = new Paragraph("HOSTELOPS RESIDENCES & PG", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXT_MUTED);
            Paragraph subtitle = new Paragraph("Official Fee Acknowledgment & Payment Receipt\nCampus Road, Knowledge Park • Phone: +91 98765 43210 • accounts@hostel.com\n\n", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            // 2. Receipt Meta Box Table (2 Columns)
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_DARK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);

            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.BOX);
            leftCell.setBorderColor(new Color(226, 232, 240));
            leftCell.setPadding(10);
            leftCell.addElement(new Paragraph("Receipt Number: REC-" + payment.getId(), boldFont));
            leftCell.addElement(new Paragraph("Transaction ID: " + payment.getTransactionId(), regularFont));
            leftCell.addElement(new Paragraph("Payment Date: " + payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a")), regularFont));
            leftCell.addElement(new Paragraph("Payment Method: " + payment.getPaymentMethod(), regularFont));
            leftCell.addElement(new Paragraph("Payment Status: " + payment.getPaymentStatus(), boldFont));
            metaTable.addCell(leftCell);

            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.BOX);
            rightCell.setBorderColor(new Color(226, 232, 240));
            rightCell.setPadding(10);
            rightCell.addElement(new Paragraph("Resident Student: " + payment.getStudent().getUser().getName(), boldFont));
            rightCell.addElement(new Paragraph("Admission No: " + payment.getStudent().getAdmissionNumber(), regularFont));
            rightCell.addElement(new Paragraph("Course: " + payment.getStudent().getCourse() + " (Yr " + payment.getStudent().getYearOfStudy() + ")", regularFont));
            rightCell.addElement(new Paragraph("Billing Month: " + fee.getMonth(), regularFont));
            metaTable.addCell(rightCell);

            document.add(metaTable);

            // 3. Itemized Fee Breakdown Table
            PdfPTable feeTable = new PdfPTable(new float[]{4, 2, 2});
            feeTable.setWidthPercentage(100);
            feeTable.setSpacingAfter(20);

            // Header Row
            addTableHeader(feeTable, "Fee Component Description", Element.ALIGN_LEFT);
            addTableHeader(feeTable, "Monthly Invoiced (₹)", Element.ALIGN_RIGHT);
            addTableHeader(feeTable, "Paid in this Txn (₹)", Element.ALIGN_RIGHT);

            // Line Items
            addTableRow(feeTable, "Room Rent & Accommodation", fee.getRoomRent().toString(), "-");
            addTableRow(feeTable, "Mess & Dining Meal Plan", fee.getMessFee().toString(), "-");
            addTableRow(feeTable, "Electricity & Power Backup", fee.getElectricityFee().toString(), "-");
            addTableRow(feeTable, "Hostel Maintenance & Facilities", fee.getMaintenanceFee().toString(), "-");

            // Totals Row
            PdfPCell totalDescCell = new PdfPCell(new Phrase("Total Invoiced Amount:", boldFont));
            totalDescCell.setPadding(8);
            totalDescCell.setBackgroundColor(HEADER_BG);
            feeTable.addCell(totalDescCell);

            PdfPCell totalAmountCell = new PdfPCell(new Phrase("₹" + fee.getTotalAmount(), boldFont));
            totalAmountCell.setPadding(8);
            totalAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalAmountCell.setBackgroundColor(HEADER_BG);
            feeTable.addCell(totalAmountCell);

            PdfPCell txnAmountCell = new PdfPCell(new Phrase("₹" + payment.getAmount(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, ACCENT_SUCCESS)));
            txnAmountCell.setPadding(8);
            txnAmountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            txnAmountCell.setBackgroundColor(HEADER_BG);
            feeTable.addCell(txnAmountCell);

            document.add(feeTable);

            // 4. Financial Summary Callout
            PdfPTable summaryTable = new PdfPTable(new float[]{3, 2});
            summaryTable.setWidthPercentage(60);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setSpacingAfter(25);

            addSummaryRow(summaryTable, "Cumulative Paid to Date:", "₹" + fee.getPaidAmount());
            addSummaryRow(summaryTable, "Remaining Balance Due:", "₹" + fee.getRemainingAmount());
            addSummaryRow(summaryTable, "Updated Invoice Status:", fee.getStatus().name());

            document.add(summaryTable);

            // 5. Verification Footer & Seal
            Paragraph notice = new Paragraph(
                    "Note: This is a computer-verified digital receipt for transaction reference " + payment.getTransactionId() +
                            ". Fees once paid are non-refundable according to hostel residential policies.\n\n",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, TEXT_MUTED)
            );
            document.add(notice);

            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);

            PdfPCell sealCell = new PdfPCell(new Phrase("[DIGITALLY VERIFIED]\nHostel Accounts Department", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, PRIMARY_COLOR)));
            sealCell.setBorder(Rectangle.NO_BORDER);
            signatureTable.addCell(sealCell);

            PdfPCell signCell = new PdfPCell(new Phrase("__________________________\nAuthorized Signatory / Warden", FontFactory.getFont(FontFactory.HELVETICA, 8, TEXT_DARK)));
            signCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            signCell.setBorder(Rectangle.NO_BORDER);
            signatureTable.addCell(signCell);

            document.add(signatureTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error creating fee receipt PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Could not generate PDF receipt: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateMonthlyFinancialReportPdf(String month) {
        log.info("Generating monthly financial report PDF for month: {}", month);

        List<Fee> fees = feeRepository.findByStatus(null); // or custom monthly query
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY_COLOR);
            Paragraph title = new Paragraph("HOSTEL MONTHLY FINANCIAL COLLECTION REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph sub = new Paragraph("Billing Cycle: " + month + " • Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")) + "\n\n", FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED));
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);

            PdfPTable table = new PdfPTable(new float[]{2, 3, 2, 2, 2, 2, 2});
            table.setWidthPercentage(100);

            addTableHeader(table, "Admission No", Element.ALIGN_LEFT);
            addTableHeader(table, "Student Name", Element.ALIGN_LEFT);
            addTableHeader(table, "Total Billed", Element.ALIGN_RIGHT);
            addTableHeader(table, "Paid Amount", Element.ALIGN_RIGHT);
            addTableHeader(table, "Balance Due", Element.ALIGN_RIGHT);
            addTableHeader(table, "Due Date", Element.ALIGN_CENTER);
            addTableHeader(table, "Status", Element.ALIGN_CENTER);

            List<Fee> monthlyFees = feeRepository.findAll().stream()
                    .filter(f -> month.equalsIgnoreCase(f.getMonth()))
                    .toList();

            BigDecimal totalBilled = BigDecimal.ZERO;
            BigDecimal totalPaid = BigDecimal.ZERO;
            BigDecimal totalDue = BigDecimal.ZERO;

            for (Fee f : monthlyFees) {
                totalBilled = totalBilled.add(f.getTotalAmount());
                totalPaid = totalPaid.add(f.getPaidAmount());
                totalDue = totalDue.add(f.getRemainingAmount());

                table.addCell(new Phrase(f.getStudent().getAdmissionNumber(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(f.getStudent().getUser().getName(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase("₹" + f.getTotalAmount(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase("₹" + f.getPaidAmount(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase("₹" + f.getRemainingAmount(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(f.getDueDate().toString(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(f.getStatus().name(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
            }

            document.add(table);

            Paragraph summary = new Paragraph(
                    String.format("\nFinancial Summary: Total Invoiced: ₹%s | Total Collected: ₹%s | Outstanding Receivables: ₹%s",
                            totalBilled, totalPaid, totalDue),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_COLOR)
            );
            document.add(summary);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating monthly financial PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Could not generate financial report PDF: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateOccupancyReportPdf() {
        log.info("Generating hostel room occupancy PDF report");

        List<Room> rooms = roomRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY_COLOR);
            Paragraph title = new Paragraph("HOSTEL RESIDENCE OCCUPANCY ROSTER", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph sub = new Paragraph("Real-Time Capacity Audit • Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")) + "\n\n", FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED));
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);

            PdfPTable table = new PdfPTable(new float[]{2, 2, 2, 2, 2, 2, 2});
            table.setWidthPercentage(100);

            addTableHeader(table, "Room No", Element.ALIGN_CENTER);
            addTableHeader(table, "Block", Element.ALIGN_CENTER);
            addTableHeader(table, "Floor", Element.ALIGN_CENTER);
            addTableHeader(table, "Room Type", Element.ALIGN_CENTER);
            addTableHeader(table, "Capacity", Element.ALIGN_CENTER);
            addTableHeader(table, "Occupied", Element.ALIGN_CENTER);
            addTableHeader(table, "Status", Element.ALIGN_CENTER);

            long totalBeds = 0;
            long occupiedBeds = 0;

            for (Room r : rooms) {
                totalBeds += r.getCapacity();
                occupiedBeds += r.getOccupied();

                table.addCell(new Phrase(r.getRoomNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
                table.addCell(new Phrase(r.getBlockName(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(String.valueOf(r.getFloor()), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(r.getRoomType().name(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(String.valueOf(r.getCapacity()), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(String.valueOf(r.getOccupied()), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(r.getStatus().name(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
            }

            document.add(table);

            double rate = totalBeds > 0 ? ((double) occupiedBeds / totalBeds) * 100.0 : 0.0;
            Paragraph summary = new Paragraph(
                    String.format("\nOccupancy Metric: Total Rooms: %d | Total Beds: %d | Occupied: %d | Available: %d | Occupancy Rate: %.2f%%",
                            rooms.size(), totalBeds, occupiedBeds, (totalBeds - occupiedBeds), rate),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_COLOR)
            );
            document.add(summary);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating occupancy PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Could not generate occupancy report PDF: " + e.getMessage());
        }
    }

    private void addTableHeader(PdfPTable table, String headerTitle, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(headerTitle, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_DARK)));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(6);
        cell.setHorizontalAlignment(align);
        cell.setBorderColor(new Color(226, 232, 240));
        table.addCell(cell);
    }

    private void addTableRow(PdfPTable table, String col1, String col2, String col3) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);

        PdfPCell c1 = new PdfPCell(new Phrase(col1, font));
        c1.setPadding(6);
        c1.setBorderColor(new Color(241, 245, 249));
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(col2, font));
        c2.setPadding(6);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setBorderColor(new Color(241, 245, 249));
        table.addCell(c2);

        PdfPCell c3 = new PdfPCell(new Phrase(col3, font));
        c3.setPadding(6);
        c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c3.setBorderColor(new Color(241, 245, 249));
        table.addCell(c3);
    }

    private void addSummaryRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
        Font valFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_DARK);

        PdfPCell lCell = new PdfPCell(new Phrase(label, labelFont));
        lCell.setBorder(Rectangle.NO_BORDER);
        lCell.setPadding(3);
        table.addCell(lCell);

        PdfPCell vCell = new PdfPCell(new Phrase(value, valFont));
        vCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vCell.setBorder(Rectangle.NO_BORDER);
        vCell.setPadding(3);
        table.addCell(vCell);
    }
}
