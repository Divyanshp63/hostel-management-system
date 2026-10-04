package com.hostel.management.service.impl;

import com.hostel.management.dto.request.CreateNoticeRequest;
import com.hostel.management.dto.request.UpdateNoticeRequest;
import com.hostel.management.dto.response.NoticeResponse;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.entity.Notice;
import com.hostel.management.entity.User;
import com.hostel.management.enums.NoticePriority;
import com.hostel.management.enums.NoticeTarget;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.NoticeRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NoticeResponse createNotice(CreateNoticeRequest request, String adminEmail) {
        User adminUser = userRepository.findByEmail(adminEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with email: " + adminEmail));

        Notice notice = Notice.builder()
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .postedBy(adminUser)
                .priority(request.getPriority())
                .targetAudience(request.getTargetAudience())
                .expiryDate(request.getExpiryDate())
                .active(true)
                .build();

        Notice saved = noticeRepository.save(notice);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public NoticeResponse updateNotice(Long id, UpdateNoticeRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with ID: " + id));

        notice.setTitle(request.getTitle().trim());
        notice.setContent(request.getContent().trim());
        notice.setPriority(request.getPriority());
        notice.setTargetAudience(request.getTargetAudience());
        notice.setExpiryDate(request.getExpiryDate());
        notice.setActive(request.getActive());

        Notice updated = noticeRepository.save(notice);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeResponse getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with ID: " + id));
        return mapToResponse(notice);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NoticeResponse> getAllNotices(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String search,
            NoticePriority priority,
            NoticeTarget target,
            Boolean active
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Notice> noticePage = noticeRepository.findNoticesWithFilters(
                search != null ? search.trim() : "",
                priority,
                target,
                active,
                pageable
        );

        List<NoticeResponse> responses = noticePage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<NoticeResponse>builder()
                .content(responses)
                .pageNumber(noticePage.getNumber())
                .pageSize(noticePage.getSize())
                .totalElements(noticePage.getTotalElements())
                .totalPages(noticePage.getTotalPages())
                .last(noticePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeResponse> getActiveNotices() {
        return noticeRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with ID: " + id));

        noticeRepository.delete(notice);
    }

    private NoticeResponse mapToResponse(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .postedByName(notice.getPostedBy().getName())
                .priority(notice.getPriority())
                .targetAudience(notice.getTargetAudience())
                .expiryDate(notice.getExpiryDate())
                .active(notice.isActive())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}
