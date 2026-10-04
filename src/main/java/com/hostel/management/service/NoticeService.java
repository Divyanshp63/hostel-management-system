package com.hostel.management.service;

import com.hostel.management.dto.request.CreateNoticeRequest;
import com.hostel.management.dto.request.UpdateNoticeRequest;
import com.hostel.management.dto.response.NoticeResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.enums.NoticePriority;
import com.hostel.management.enums.NoticeTarget;

import java.util.List;

public interface NoticeService {

    NoticeResponse createNotice(CreateNoticeRequest request, String adminEmail);

    NoticeResponse updateNotice(Long id, UpdateNoticeRequest request);

    NoticeResponse getNoticeById(Long id);

    PageResponse<NoticeResponse> getAllNotices(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            NoticePriority priority,
            NoticeTarget target,
            Boolean active
    );

    List<NoticeResponse> getActiveNotices();

    void deleteNotice(Long id);
}
