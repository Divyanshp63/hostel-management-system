package com.hostel.management.config;

import com.hostel.management.entity.*;
import com.hostel.management.enums.*;
import com.hostel.management.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final RoomAllocationRepository roomAllocationRepository;
    private final MessMenuRepository messMenuRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        cleanOldAdmin();
        seedRooms();
        seedWardenUser();
        seedAccountantUser();
        seedComplaintStaffUser();
        seedStudentUser();
        seedDefaultMessMenu();
    }

    private void cleanOldAdmin() {
        userRepository.findByEmail("admin@hostel.com").ifPresent(user -> {
            user.setEnabled(false);
            userRepository.save(user);
        });
    }

    private void seedRooms() {
        if (roomRepository.count() == 0) {
            log.info("Seeding initial hostel rooms...");
            List<Room> initialRooms = List.of(
                    Room.builder()
                            .roomNumber("A-101")
                            .blockName("Block A")
                            .floor(1)
                            .roomType(RoomType.DOUBLE)
                            .capacity(2)
                            .occupied(0)
                            .rentPerMonth(new BigDecimal("6000.00"))
                            .status(RoomStatus.AVAILABLE)
                            .description("Double sharing room with attached balcony and study desks")
                            .build(),
                    Room.builder()
                            .roomNumber("A-102")
                            .blockName("Block A")
                            .floor(1)
                            .roomType(RoomType.SINGLE)
                            .capacity(1)
                            .occupied(0)
                            .rentPerMonth(new BigDecimal("8500.00"))
                            .status(RoomStatus.AVAILABLE)
                            .description("Single room with AC and attached washroom")
                            .build(),
                    Room.builder()
                            .roomNumber("B-201")
                            .blockName("Block B")
                            .floor(2)
                            .roomType(RoomType.DOUBLE)
                            .capacity(2)
                            .occupied(0)
                            .rentPerMonth(new BigDecimal("5500.00"))
                            .status(RoomStatus.AVAILABLE)
                            .description("Double room on second floor with high-speed Wi-Fi")
                            .build(),
                    Room.builder()
                            .roomNumber("B-202")
                            .blockName("Block B")
                            .floor(2)
                            .roomType(RoomType.TRIPLE)
                            .capacity(3)
                            .occupied(0)
                            .rentPerMonth(new BigDecimal("4500.00"))
                            .status(RoomStatus.AVAILABLE)
                            .description("Triple sharing room with spacious storage lockers")
                            .build()
            );
            roomRepository.saveAll(initialRooms);
            log.info("Initial rooms seeded successfully.");
        }
    }

    private void seedWardenUser() {
        String wardenEmail = "warden@smarthostel.com";
        User warden = userRepository.findByEmail(wardenEmail).orElseGet(() -> User.builder()
                .name("Chief Warden")
                .email(wardenEmail)
                .phone("9876543210")
                .role(Role.WARDEN)
                .build());
        warden.setPassword(passwordEncoder.encode("Warden@123"));
        warden.setRole(Role.WARDEN);
        warden.setName("Chief Warden");
        warden.setEnabled(true);
        warden = userRepository.save(warden);

        if (!staffRepository.existsByUser(warden)) {
            Staff staff = Staff.builder()
                    .user(warden)
                    .employeeId("WRD-001")
                    .designation("Hostel Warden")
                    .hostelAssignment("Main Hostel Campus")
                    .build();
            staffRepository.save(staff);
        }
        log.info("Warden account verified: {}", wardenEmail);
    }

    private void seedAccountantUser() {
        String accountantEmail = "accountant@smarthostel.com";
        User accountant = userRepository.findByEmail(accountantEmail).orElseGet(() -> User.builder()
                .name("Hostel Accountant")
                .email(accountantEmail)
                .phone("9876543211")
                .role(Role.ACCOUNTANT)
                .build());
        accountant.setPassword(passwordEncoder.encode("Accountant@123"));
        accountant.setRole(Role.ACCOUNTANT);
        accountant.setName("Hostel Accountant");
        accountant.setEnabled(true);
        accountant = userRepository.save(accountant);

        if (!staffRepository.existsByUser(accountant)) {
            Staff staff = Staff.builder()
                    .user(accountant)
                    .employeeId("ACC-001")
                    .designation("Chief Accountant")
                    .build();
            staffRepository.save(staff);
        }
        log.info("Accountant account verified: {}", accountantEmail);
    }

    private void seedComplaintStaffUser() {
        String complaintEmail = "complaint@smarthostel.com";
        User staffUser = userRepository.findByEmail(complaintEmail).orElseGet(() -> User.builder()
                .name("Maintenance Staff")
                .email(complaintEmail)
                .phone("9876543215")
                .role(Role.COMPLAINT_STAFF)
                .build());
        staffUser.setPassword(passwordEncoder.encode("Complaint@123"));
        staffUser.setRole(Role.COMPLAINT_STAFF);
        staffUser.setName("Maintenance Staff");
        staffUser.setEnabled(true);
        final User savedStaffUser = userRepository.save(staffUser);

        Staff staff = staffRepository.findByUser(savedStaffUser).orElseGet(() -> Staff.builder()
                .user(savedStaffUser)
                .build());
        staff.setEmployeeId("CMP-001");
        staff.setDepartment(ComplaintCategory.MAINTENANCE);
        staff.setDesignation("Maintenance Lead / Technician");
        staffRepository.save(staff);

        log.info("Complaint / Maintenance Staff account verified: {}", complaintEmail);
    }

    private void seedStudentUser() {
        String studentEmail = "student@smarthostel.com";
        User studentUser = userRepository.findByEmail(studentEmail).orElseGet(() -> User.builder()
                .name("Student Resident")
                .email(studentEmail)
                .phone("9876543212")
                .role(Role.STUDENT)
                .build());
        studentUser.setPassword(passwordEncoder.encode("Student@123"));
        studentUser.setRole(Role.STUDENT);
        studentUser.setName("Student Resident");
        studentUser.setEnabled(true);
        final User savedStudentUser = userRepository.save(studentUser);

        Student student = studentRepository.findByUser(savedStudentUser).orElseGet(() -> Student.builder()
                .user(savedStudentUser)
                .admissionNumber("STU-2026-001")
                .college("Apex Engineering Institute")
                .course("B.Tech Computer Science")
                .yearOfStudy("3")
                .gender(Gender.MALE)
                .hostelName("Block A")
                .guardianName("Ramesh Kumar")
                .guardianPhone("9876500001")
                .emergencyContact("9876500001")
                .address("104 Park Avenue, City Center")
                .build());
        student = studentRepository.save(student);

        // Ensure student has room allocation
        Room room = roomRepository.findByRoomNumber("A-101").orElse(null);
        if (room != null && !roomAllocationRepository.existsByStudentIdAndStatus(student.getId(), AllocationStatus.ACTIVE)) {
            RoomAllocation allocation = RoomAllocation.builder()
                    .student(student)
                    .room(room)
                    .bedNumber("Bed 1")
                    .status(AllocationStatus.ACTIVE)
                    .requestDate(LocalDate.now())
                    .startDate(LocalDate.now())
                    .remarks("Initial resident allocation")
                    .build();
            roomAllocationRepository.save(allocation);

            room.setOccupied(1);
            room.updateStatusBasedOnOccupancy();
            roomRepository.save(room);
        }
        log.info("Student account verified: {}", studentEmail);
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
