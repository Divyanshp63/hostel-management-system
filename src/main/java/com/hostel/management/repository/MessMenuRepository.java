package com.hostel.management.repository;

import com.hostel.management.entity.MessMenu;
import com.hostel.management.enums.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessMenuRepository extends JpaRepository<MessMenu, Long> {

    Optional<MessMenu> findByDayOfWeekAndMealType(DayOfWeek dayOfWeek, MealType mealType);

    List<MessMenu> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<MessMenu> findByDayOfWeekOrderByMealTypeAsc(DayOfWeek dayOfWeek);

    List<MessMenu> findAllByOrderByDayOfWeekAscMealTypeAsc();

    boolean existsByDayOfWeekAndMealType(DayOfWeek dayOfWeek, MealType mealType);

    boolean existsByDayOfWeekAndMealTypeAndIdNot(DayOfWeek dayOfWeek, MealType mealType, Long id);

    void deleteByDayOfWeek(DayOfWeek dayOfWeek);
}
