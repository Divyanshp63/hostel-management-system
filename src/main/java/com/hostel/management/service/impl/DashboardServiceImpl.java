package com.hostel.management.service.impl;

import com.hostel.management.dto.response.*;
import com.hostel.management.entity.*;
import com.hostel.management.enums.*;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.*;
import com.hostel.management.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final RoomAllocationRepository roomAllocationRepository;
    private final FeeRepository feeRepository;
    private final PaymentRepository paymentRepository;
    private final ComplaintRepository complaintRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final VisitorRequestRepository visitorRequestRepository;
    private final NoticeRepository noticeRepository;
    private final MessMenuRepository messMenuRepository;

    @Override
    public AdminDashboardResponse getAdminDashboardStats() {
        log.info("Computing complete Admin Dashboard KPI and analytics");

        // 1. Capacity & Occupancy KPI
        long totalRooms = roomRepository.count();
        long totalBeds = roomRepository.sumTotalCapacity();
        long occupiedBeds = roomRepository.sumTotalOccupied();
        long availableBeds = Math.max(0, totalBeds - occupiedBeds);
        double occupancyRate = totalBeds > 0 
                ? Math.round(((double) occupiedBeds / totalBeds) * 10000.0) / 100.0 
                : 0.0;

        // 2. Student & Allocation Stats
        long totalStudents = studentRepository.count();
        long activeAllocations = roomAllocationRepository.countByStatus(AllocationStatus.ACTIVE) + roomAllocationRepository.countByStatus(AllocationStatus.APPROVED);
        long pendingAllocations = roomAllocationRepository.countByStatus(AllocationStatus.PENDING);

        // 3. Financial KPI
        BigDecimal totalFeeBilled = feeRepository.sumTotalBilled();
        BigDecimal totalFeeCollected = paymentRepository.sumSuccessfulPayments();
        BigDecimal totalFeePending = feeRepository.sumTotalRemaining();
        long overdueFeeCount = feeRepository.countByStatus(FeeStatus.OVERDUE);

        // 4. Operations & Welfare Stats
        long pendingComplaints = complaintRepository.countByStatus(ComplaintStatus.PENDING);
        long inProgressComplaints = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolvedComplaints = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long pendingLeaves = leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        long activeVisitorsToday = visitorRequestRepository.countByVisitDate(LocalDate.now());
        long activeNoticesCount = noticeRepository.countByActiveTrue();

        // 5. Monthly Revenue Analytics (Last 6 Months from successful payments)
        List<Payment> successfulPayments = paymentRepository.findByPaymentStatusOrderByPaymentDateDesc(PaymentStatus.SUCCESS);
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, BigDecimal> revenueByMonth = new TreeMap<>();

        for (Payment payment : successfulPayments) {
            String month = payment.getPaymentDate().format(monthFormatter);
            revenueByMonth.put(month, revenueByMonth.getOrDefault(month, BigDecimal.ZERO).add(payment.getAmount()));
        }

        List<MonthlyRevenueDto> monthlyRevenue = revenueByMonth.entrySet().stream()
                .map(entry -> MonthlyRevenueDto.builder()
                        .month(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        // 6. Complaints by Category Breakdown
        Map<String, Long> complaintsByCategory = new LinkedHashMap<>();
        for (ComplaintCategory category : ComplaintCategory.values()) {
            complaintsByCategory.put(category.name(), 0L);
        }
        List<Object[]> categoryCounts = complaintRepository.countComplaintsByCategory();
        for (Object[] row : categoryCounts) {
            if (row[0] != null) {
                complaintsByCategory.put(row[0].toString(), (Long) row[1]);
            }
        }

        // 7. Room Type Occupancy Breakdown
        List<RoomTypeOccupancyDto> roomTypeOccupancy = new ArrayList<>();
        List<Object[]> typeStats = roomRepository.getRoomOccupancyByType();
        for (Object[] row : typeStats) {
            String typeName = row[0].toString();
            long roomCount = (Long) row[1];
            long cap = (Long) row[2];
            long occ = (Long) row[3];
            double rate = cap > 0 ? Math.round(((double) occ / cap) * 10000.0) / 100.0 : 0.0;

            roomTypeOccupancy.add(RoomTypeOccupancyDto.builder()
                    .roomType(typeName)
                    .roomCount(roomCount)
                    .totalBeds(cap)
                    .occupiedBeds(occ)
                    .occupancyRate(rate)
                    .build());
        }

        // 8. Recent Complaints Feed (Top 5)
        List<RecentComplaintDto> recentComplaints = complaintRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(c -> RecentComplaintDto.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .studentName(c.getStudent().getUser().getName())
                        .roomNumber(c.getRoom() != null ? c.getRoom().getRoomNumber() : "N/A")
                        .category(c.getCategory().name())
                        .status(c.getStatus().name())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // 9. Pending Allocation Requests Feed (Top 5)
        List<RecentAllocationDto> pendingAllocationRequests = roomAllocationRepository
                .findTop5ByStatusOrderByCreatedAtDesc(AllocationStatus.PENDING)
                .stream()
                .map(a -> RecentAllocationDto.builder()
                        .id(a.getId())
                        .studentName(a.getStudent().getUser().getName())
                        .admissionNumber(a.getStudent().getAdmissionNumber())
                        .roomNumber(a.getRoom().getRoomNumber())
                        .requestedAt(a.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalRooms(totalRooms)
                .totalBeds(totalBeds)
                .occupiedBeds(occupiedBeds)
                .availableBeds(availableBeds)
                .occupancyRate(occupancyRate)
                .totalStudents(totalStudents)
                .activeAllocations(activeAllocations)
                .pendingAllocations(pendingAllocations)
                .totalFeeBilled(totalFeeBilled)
                .totalFeeCollected(totalFeeCollected)
                .totalFeePending(totalFeePending)
                .overdueFeeCount(overdueFeeCount)
                .pendingComplaints(pendingComplaints)
                .inProgressComplaints(inProgressComplaints)
                .resolvedComplaints(resolvedComplaints)
                .pendingLeaves(pendingLeaves)
                .activeVisitorsToday(activeVisitorsToday)
                .activeNoticesCount(activeNoticesCount)
                .monthlyRevenue(monthlyRevenue)
                .complaintsByCategory(complaintsByCategory)
                .roomTypeOccupancy(roomTypeOccupancy)
                .recentComplaints(recentComplaints)
                .pendingAllocationRequests(pendingAllocationRequests)
                .build();
    }

    @Override
    public StudentDashboardResponse getStudentDashboardStats(String userEmail) {
        log.info("Computing personalized Student Dashboard for user: {}", userEmail);

        Student student = studentRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + userEmail));

        Long studentId = student.getId();

        // 1. Room Allocation Details
        String roomNumber = "Not Allocated";
        String roomType = "N/A";
        String allocationStatus = "NONE";

        Optional<RoomAllocation> activeAlloc = roomAllocationRepository.findByStudentIdAndStatus(studentId, AllocationStatus.ACTIVE);
        if (activeAlloc.isEmpty()) {
            activeAlloc = roomAllocationRepository.findByStudentIdAndStatus(studentId, AllocationStatus.APPROVED);
        }
        if (activeAlloc.isPresent()) {
            RoomAllocation alloc = activeAlloc.get();
            roomNumber = alloc.getRoom().getRoomNumber();
            roomType = alloc.getRoom().getRoomType().name();
            allocationStatus = "APPROVED";
        } else {
            Optional<RoomAllocation> pendingAlloc = roomAllocationRepository.findByStudentIdAndStatus(studentId, AllocationStatus.PENDING);
            if (pendingAlloc.isPresent()) {
                roomNumber = pendingAlloc.get().getRoom().getRoomNumber();
                roomType = pendingAlloc.get().getRoom().getRoomType().name();
                allocationStatus = "PENDING";
            }
        }

        // 2. Financial Overview
        BigDecimal totalPendingDues = feeRepository.sumRemainingByStudentId(studentId);
        BigDecimal lastPaymentAmount = BigDecimal.ZERO;
        java.time.LocalDateTime lastPaymentDate = null;

        Optional<Payment> lastPayment = paymentRepository.findFirstByStudentIdAndPaymentStatusOrderByPaymentDateDesc(studentId, PaymentStatus.SUCCESS);
        if (lastPayment.isPresent()) {
            lastPaymentAmount = lastPayment.get().getAmount();
            lastPaymentDate = lastPayment.get().getPaymentDate();
        }

        String latestFeeStatus = "NO_FEES";
        String latestFeeMonth = "N/A";
        Optional<Fee> latestFee = feeRepository.findFirstByStudentIdOrderByCreatedAtDesc(studentId);
        if (latestFee.isPresent()) {
            latestFeeStatus = latestFee.get().getStatus().name();
            latestFeeMonth = latestFee.get().getMonth();
        }

        // 3. Today's Mess Menu
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<MessMenu> todayMenus = messMenuRepository.findByDayOfWeek(today);

        String todayBreakfast = "Not Scheduled";
        String todayLunch = "Not Scheduled";
        String todaySnacks = "Not Scheduled";
        String todayDinner = "Not Scheduled";

        for (MessMenu m : todayMenus) {
            if (m.getMealType() == MealType.BREAKFAST) {
                todayBreakfast = m.getItems() + (m.getTiming() != null ? " (" + m.getTiming() + ")" : "");
            } else if (m.getMealType() == MealType.LUNCH) {
                todayLunch = m.getItems() + (m.getTiming() != null ? " (" + m.getTiming() + ")" : "");
            } else if (m.getMealType() == MealType.SNACKS) {
                todaySnacks = m.getItems() + (m.getTiming() != null ? " (" + m.getTiming() + ")" : "");
            } else if (m.getMealType() == MealType.DINNER) {
                todayDinner = m.getItems() + (m.getTiming() != null ? " (" + m.getTiming() + ")" : "");
            }
        }

        // 4. Welfare & Request Counts
        long myPendingComplaints = complaintRepository.countByStudentIdAndStatus(studentId, ComplaintStatus.PENDING);
        String latestComplaintStatus = "NONE";
        Optional<Complaint> lastComplaint = complaintRepository.findFirstByStudentIdOrderByCreatedAtDesc(studentId);
        if (lastComplaint.isPresent()) {
            latestComplaintStatus = lastComplaint.get().getStatus().name();
        }

        long myPendingLeaves = leaveRequestRepository.countByStudentIdAndStatus(studentId, LeaveStatus.PENDING);
        long myPendingVisitors = visitorRequestRepository.countByStudentIdAndStatus(studentId, VisitorStatus.PENDING);

        // 5. Latest 3 Active Notices
        List<NoticeResponse> latestNotices = noticeRepository.findTop3ByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(n -> NoticeResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .content(n.getContent())
                        .priority(n.getPriority())
                        .targetAudience(n.getTargetAudience())
                        .active(n.isActive())
                        .createdAt(n.getCreatedAt())
                        .updatedAt(n.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return StudentDashboardResponse.builder()
                .studentId(studentId)
                .studentName(student.getUser().getName())
                .admissionNumber(student.getAdmissionNumber())
                .roomNumber(roomNumber)
                .roomType(roomType)
                .allocationStatus(allocationStatus)
                .totalPendingDues(totalPendingDues)
                .lastPaymentAmount(lastPaymentAmount)
                .lastPaymentDate(lastPaymentDate)
                .latestFeeStatus(latestFeeStatus)
                .latestFeeMonth(latestFeeMonth)
                .todayBreakfast(todayBreakfast)
                .todayLunch(todayLunch)
                .todaySnacks(todaySnacks)
                .todayDinner(todayDinner)
                .myPendingComplaints(myPendingComplaints)
                .latestComplaintStatus(latestComplaintStatus)
                .myPendingLeaves(myPendingLeaves)
                .myPendingVisitors(myPendingVisitors)
                .latestNotices(latestNotices)
                .build();
    }
}
