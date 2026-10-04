package com.hostel.management.service.impl;

import com.hostel.management.dto.request.MessMenuRequest;
import com.hostel.management.dto.response.MessMenuResponse;
import com.hostel.management.dto.response.WeeklyMenuResponse;
import com.hostel.management.entity.MessMenu;
import com.hostel.management.exception.DuplicateResourceException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.MessMenuRepository;
import com.hostel.management.service.MessMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MessMenuServiceImpl implements MessMenuService {

    private final MessMenuRepository messMenuRepository;

    @Override
    @Transactional
    public MessMenuResponse createMenu(MessMenuRequest request) {
        log.info("Creating mess menu for {} - {}", request.getDayOfWeek(), request.getMealType());

        if (messMenuRepository.existsByDayOfWeekAndMealType(request.getDayOfWeek(), request.getMealType())) {
            throw new DuplicateResourceException(
                    "Mess menu already configured for " + request.getDayOfWeek() + " (" + request.getMealType() + "). Please update existing record or use upsert."
            );
        }

        MessMenu menu = MessMenu.builder()
                .dayOfWeek(request.getDayOfWeek())
                .mealType(request.getMealType())
                .items(request.getItems().trim())
                .timing(request.getTiming() != null ? request.getTiming().trim() : null)
                .specialDiet(request.getSpecialDiet() != null ? request.getSpecialDiet().trim() : null)
                .build();

        MessMenu savedMenu = messMenuRepository.save(menu);
        log.info("Mess menu created successfully with ID: {}", savedMenu.getId());
        return mapToResponse(savedMenu);
    }

    @Override
    @Transactional
    public MessMenuResponse createOrUpdateMenu(MessMenuRequest request) {
        log.info("Upserting mess menu for {} - {}", request.getDayOfWeek(), request.getMealType());

        MessMenu menu = messMenuRepository.findByDayOfWeekAndMealType(request.getDayOfWeek(), request.getMealType())
                .orElse(MessMenu.builder()
                        .dayOfWeek(request.getDayOfWeek())
                        .mealType(request.getMealType())
                        .build());

        menu.setItems(request.getItems().trim());
        menu.setTiming(request.getTiming() != null ? request.getTiming().trim() : null);
        menu.setSpecialDiet(request.getSpecialDiet() != null ? request.getSpecialDiet().trim() : null);

        MessMenu savedMenu = messMenuRepository.save(menu);
        log.info("Mess menu upserted successfully with ID: {}", savedMenu.getId());
        return mapToResponse(savedMenu);
    }

    @Override
    @Transactional
    public MessMenuResponse updateMenu(Long id, MessMenuRequest request) {
        log.info("Updating mess menu with ID: {}", id);

        MessMenu menu = messMenuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mess menu not found with ID: " + id));

        if (messMenuRepository.existsByDayOfWeekAndMealTypeAndIdNot(request.getDayOfWeek(), request.getMealType(), id)) {
            throw new DuplicateResourceException(
                    "Another menu already exists for " + request.getDayOfWeek() + " (" + request.getMealType() + ")"
            );
        }

        menu.setDayOfWeek(request.getDayOfWeek());
        menu.setMealType(request.getMealType());
        menu.setItems(request.getItems().trim());
        menu.setTiming(request.getTiming() != null ? request.getTiming().trim() : null);
        menu.setSpecialDiet(request.getSpecialDiet() != null ? request.getSpecialDiet().trim() : null);

        MessMenu updatedMenu = messMenuRepository.save(menu);
        log.info("Mess menu updated successfully with ID: {}", updatedMenu.getId());
        return mapToResponse(updatedMenu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        log.info("Deleting mess menu with ID: {}", id);
        MessMenu menu = messMenuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mess menu not found with ID: " + id));
        messMenuRepository.delete(menu);
        log.info("Mess menu deleted successfully with ID: {}", id);
    }

    @Override
    public MessMenuResponse getMenuById(Long id) {
        MessMenu menu = messMenuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mess menu not found with ID: " + id));
        return mapToResponse(menu);
    }

    @Override
    public List<MessMenuResponse> getMenuForDay(DayOfWeek dayOfWeek) {
        return messMenuRepository.findByDayOfWeekOrderByMealTypeAsc(dayOfWeek)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessMenuResponse> getTodayMenu() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        return getMenuForDay(today);
    }

    @Override
    public WeeklyMenuResponse getWeeklyMenu() {
        Map<String, List<MessMenuResponse>> weeklySchedule = new LinkedHashMap<>();
        int totalMeals = 0;

        for (DayOfWeek day : DayOfWeek.values()) {
            List<MessMenuResponse> dayMenus = getMenuForDay(day);
            weeklySchedule.put(day.name(), dayMenus);
            totalMeals += dayMenus.size();
        }

        return WeeklyMenuResponse.builder()
                .weeklySchedule(weeklySchedule)
                .totalMealsConfigured(totalMeals)
                .build();
    }

    @Override
    public List<MessMenuResponse> getAllMenus() {
        return messMenuRepository.findAllByOrderByDayOfWeekAscMealTypeAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<MessMenuResponse> bulkSaveMenu(List<MessMenuRequest> requests) {
        log.info("Bulk saving {} mess menu records", requests.size());
        List<MessMenuResponse> responses = new ArrayList<>();
        for (MessMenuRequest req : requests) {
            responses.add(createOrUpdateMenu(req));
        }
        return responses;
    }

    private MessMenuResponse mapToResponse(MessMenu menu) {
        return MessMenuResponse.builder()
                .id(menu.getId())
                .dayOfWeek(menu.getDayOfWeek())
                .mealType(menu.getMealType())
                .items(menu.getItems())
                .timing(menu.getTiming())
                .specialDiet(menu.getSpecialDiet())
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }
}
