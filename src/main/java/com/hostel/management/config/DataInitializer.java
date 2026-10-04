package com.hostel.management.config;

import com.hostel.management.entity.MessMenu;
import com.hostel.management.entity.User;
import com.hostel.management.enums.MealType;
import com.hostel.management.enums.Role;
import com.hostel.management.repository.MessMenuRepository;
import com.hostel.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MessMenuRepository messMenuRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedAccountantUser();
        seedStudentUsers();
        seedDefaultMessMenu();
    }

    private void seedAdminUser() {
        String adminEmail = "admin@hostel.com";
        User admin = userRepository.findByEmail(adminEmail).orElseGet(() -> User.builder()
                .name("Hostel Administrator")
                .email(adminEmail)
                .phone("9876543210")
                .role(Role.ADMIN)
                .build());
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setEnabled(true);
        userRepository.save(admin);
        log.info("Default ADMIN user ready: {} / Admin@123", adminEmail);
    }

    private void seedAccountantUser() {
        String accountantEmail = "accountant@hostel.com";
        User accountant = userRepository.findByEmail(accountantEmail).orElseGet(() -> User.builder()
                .name("Hostel Accountant")
                .email(accountantEmail)
                .phone("9876543211")
                .role(Role.ACCOUNTANT)
                .build());
        accountant.setPassword(passwordEncoder.encode("Accountant@123"));
        accountant.setEnabled(true);
        userRepository.save(accountant);
        log.info("Default ACCOUNTANT user ready: {} / Accountant@123", accountantEmail);
    }

    private void seedStudentUsers() {
        String encodedStudentPass = passwordEncoder.encode("Student@123");
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .toList();

        for (User s : students) {
            if (s.getPassword() == null || s.getPassword().isEmpty() || "aarav.patel@hostel.com".equalsIgnoreCase(s.getEmail())) {
                s.setPassword(encodedStudentPass);
                s.setEnabled(true);
                userRepository.save(s);
            }
        }
        log.info("Default STUDENT credentials initialized: aarav.patel@hostel.com / Student@123");
    }

    private void seedDefaultMessMenu() {
        if (messMenuRepository.count() == 0) {
            log.info("Seeding default 7-day hostel mess timetable...");
            List<MessMenu> menus = new ArrayList<>();

            // MONDAY
            menus.add(createMenu(DayOfWeek.MONDAY, MealType.BREAKFAST, "Aloo Poha, Jalebi, Sprouts, Tea/Coffee", "08:00 AM - 09:30 AM", "Boiled eggs available"));
            menus.add(createMenu(DayOfWeek.MONDAY, MealType.LUNCH, "Dal Tadka, Mix Veg Sabzi, Jeera Rice, Phulka, Boondi Raita, Salad", "12:30 PM - 02:30 PM", "Fresh salad & pickle"));
            menus.add(createMenu(DayOfWeek.MONDAY, MealType.SNACKS, "Samosa, Mint Chutney, Adrak Chai", "05:00 PM - 06:00 PM", null));
            menus.add(createMenu(DayOfWeek.MONDAY, MealType.DINNER, "Paneer Butter Masala / Chicken Curry, Tandoori Roti, Rice, Gulab Jamun", "08:00 PM - 10:00 PM", "Special dessert"));

            // TUESDAY
            menus.add(createMenu(DayOfWeek.TUESDAY, MealType.BREAKFAST, "Idli, Medu Vada, Coconut Chutney, Sambar, Filter Coffee", "08:00 AM - 09:30 AM", "South Indian Special"));
            menus.add(createMenu(DayOfWeek.TUESDAY, MealType.LUNCH, "Rajma Masala, Aloo Gobi, Steamed Basmati Rice, Chapati, Dahi, Papad", "12:30 PM - 02:30 PM", "Punjabi Rajma Chawal"));
            menus.add(createMenu(DayOfWeek.TUESDAY, MealType.SNACKS, "Bread Pakora, Tomato Sauce, Tea", "05:00 PM - 06:00 PM", null));
            menus.add(createMenu(DayOfWeek.TUESDAY, MealType.DINNER, "Dal Makhani, Bhindi Fry, Laccha Paratha, Peas Pulao, Rice Kheer", "08:00 PM - 10:00 PM", "Traditional Kheer"));

            // WEDNESDAY
            menus.add(createMenu(DayOfWeek.WEDNESDAY, MealType.BREAKFAST, "Aloo Paratha with Amul Butter, Dahi, Mixed Pickle, Tea/Coffee", "08:00 AM - 09:30 AM", "Extra curd available"));
            menus.add(createMenu(DayOfWeek.WEDNESDAY, MealType.LUNCH, "Chole Masala, Bhature / Rice, Sirka Pyaaz, Green Chutney, Lassi", "12:30 PM - 02:30 PM", "Chilled Sweet Lassi"));
            menus.add(createMenu(DayOfWeek.WEDNESDAY, MealType.SNACKS, "Grilled Veg Sandwich, Hot Coffee", "05:00 PM - 06:00 PM", null));
            menus.add(createMenu(DayOfWeek.WEDNESDAY, MealType.DINNER, "Kadai Paneer / Egg Curry, Tawa Roti, Veg Pulao, Moong Dal Halwa", "08:00 PM - 10:00 PM", "Rich Halwa"));

            // THURSDAY
            menus.add(createMenu(DayOfWeek.THURSDAY, MealType.BREAKFAST, "Masala Dosa, Onion Uttapam, Coconut & Tomato Chutney, Sambar, Tea", "08:00 AM - 09:30 AM", "Crispy Dosa"));
            menus.add(createMenu(DayOfWeek.THURSDAY, MealType.LUNCH, "Rajasthani Kadhi Pakora, Aloo Jeera, Steamed Rice, Phulka, Papad", "12:30 PM - 02:30 PM", "Pure Vegetarian"));
            menus.add(createMenu(DayOfWeek.THURSDAY, MealType.SNACKS, "Bhel Puri, Masala Chai", "05:00 PM - 06:00 PM", null));
            menus.add(createMenu(DayOfWeek.THURSDAY, MealType.DINNER, "Shahi Paneer, Dal Fry, Tawa Butter Roti, Jeera Rice, Fruit Custard", "08:00 PM - 10:00 PM", "Chilled Custard"));

            // FRIDAY
            menus.add(createMenu(DayOfWeek.FRIDAY, MealType.BREAKFAST, "Bedmi Puri, Aloo Bhaji, Suji Halwa, Banana, Tea/Coffee", "08:00 AM - 09:30 AM", "Fresh seasonal fruit"));
            menus.add(createMenu(DayOfWeek.FRIDAY, MealType.LUNCH, "Veg Dum Biryani, Mirchi Ka Salan, Cucumber Raita, Papad, Roasted Peanut", "12:30 PM - 02:30 PM", "Hyderabadi style"));
            menus.add(createMenu(DayOfWeek.FRIDAY, MealType.SNACKS, "Veg Hakka Noodles / Pasta, Cold Coffee", "05:00 PM - 06:00 PM", null));
            menus.add(createMenu(DayOfWeek.FRIDAY, MealType.DINNER, "Matar Paneer, Yellow Dal Tadka, Ghee Phulka, Steamed Rice, Rasgulla", "08:00 PM - 10:00 PM", "Sweet Bengali Rasgulla"));

            // SATURDAY
            menus.add(createMenu(DayOfWeek.SATURDAY, MealType.BREAKFAST, "Gobhi & Methi Paratha, White Butter, Curd, Lemon Pickle, Tea", "08:00 AM - 09:30 AM", null));
            menus.add(createMenu(DayOfWeek.SATURDAY, MealType.LUNCH, "Panchmel Dal, Baingan Bharta, Tawa Roti, Steamed Rice, Onion Kachumber", "12:30 PM - 02:30 PM", null));
            menus.add(createMenu(DayOfWeek.SATURDAY, MealType.SNACKS, "Mumbai Pav Bhaji, Masala Chai", "05:00 PM - 06:00 PM", "Extra butter pav"));
            menus.add(createMenu(DayOfWeek.SATURDAY, MealType.DINNER, "Handi Paneer / Chicken Masala, Butter Naan, Jeera Rice, Jalebi with Rabdi", "08:00 PM - 10:00 PM", "Weekend Special"));

            // SUNDAY
            menus.add(createMenu(DayOfWeek.SUNDAY, MealType.BREAKFAST, "Chole Kulche / Bread Omelette, Fresh Orange Juice, Tea/Coffee", "08:30 AM - 10:30 AM", "Relaxed Sunday Timings"));
            menus.add(createMenu(DayOfWeek.SUNDAY, MealType.LUNCH, "Grand Feast: Paneer Lababdar / Mutton Biryani, Roomali Roti, Pulao, Ice Cream", "12:30 PM - 03:00 PM", "Sunday Grand Feast"));
            menus.add(createMenu(DayOfWeek.SUNDAY, MealType.SNACKS, "Assorted Cookies / Rusks, Cardamom Tea", "05:00 PM - 06:00 PM", null));
            menus.add(createMenu(DayOfWeek.SUNDAY, MealType.DINNER, "Light Supper: Moong Dal Khichdi, Gujarati Kadhi, Aloo Chokha, Papad, Dahi", "08:00 PM - 10:00 PM", "Comfort digestive meal"));

            messMenuRepository.saveAll(menus);
            log.info("Default 7-day mess timetable (28 meals) seeded successfully!");
        }
    }

    private MessMenu createMenu(DayOfWeek day, MealType type, String items, String timing, String specialDiet) {
        return MessMenu.builder()
                .dayOfWeek(day)
                .mealType(type)
                .items(items)
                .timing(timing)
                .specialDiet(specialDiet)
                .build();
    }
}
