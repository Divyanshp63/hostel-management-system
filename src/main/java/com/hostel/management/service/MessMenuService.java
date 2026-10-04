package com.hostel.management.service;

import com.hostel.management.dto.request.MessMenuRequest;
import com.hostel.management.dto.response.MessMenuResponse;
import com.hostel.management.dto.response.WeeklyMenuResponse;

import java.time.DayOfWeek;
import java.util.List;

public interface MessMenuService {

    MessMenuResponse createMenu(MessMenuRequest request);

    MessMenuResponse createOrUpdateMenu(MessMenuRequest request);

    MessMenuResponse updateMenu(Long id, MessMenuRequest request);

    void deleteMenu(Long id);

    MessMenuResponse getMenuById(Long id);

    List<MessMenuResponse> getMenuForDay(DayOfWeek dayOfWeek);

    List<MessMenuResponse> getTodayMenu();

    WeeklyMenuResponse getWeeklyMenu();

    List<MessMenuResponse> getAllMenus();

    List<MessMenuResponse> bulkSaveMenu(List<MessMenuRequest> requests);
}
