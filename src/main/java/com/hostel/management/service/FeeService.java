package com.hostel.management.service;

import com.hostel.management.dto.request.BulkFeeGenerationRequest;
import com.hostel.management.dto.request.CreateFeeRequest;
import com.hostel.management.dto.request.UpdateFeeRequest;
import com.hostel.management.dto.response.FeeResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.enums.FeeStatus;

import java.util.List;

public interface FeeService {

    FeeResponse createFee(CreateFeeRequest request);

    List<FeeResponse> generateBulkFees(BulkFeeGenerationRequest request);

    FeeResponse updateFee(Long id, UpdateFeeRequest request);

    FeeResponse getFeeById(Long id, String currentUserEmail);

    PageResponse<FeeResponse> getAllFees(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            FeeStatus status,
            String month
    );

    List<FeeResponse> getMyFees(String currentUserEmail);

    List<FeeResponse> getFeesByStudentId(Long studentId);

    void deleteFee(Long id);
}
