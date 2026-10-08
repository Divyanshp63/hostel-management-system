package com.hostel.management.service.impl;

import com.hostel.management.dto.request.LoginRequest;
import com.hostel.management.dto.request.RegisterRequest;
import com.hostel.management.dto.request.UserRegistrationDto;
import com.hostel.management.dto.response.AuthResponse;
import com.hostel.management.dto.response.UserProfileResponse;
import com.hostel.management.entity.*;
import com.hostel.management.enums.AllocationStatus;
import com.hostel.management.enums.Role;
import com.hostel.management.enums.RoomStatus;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.exception.DuplicateResourceException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.*;
import com.hostel.management.security.JwtService;
import com.hostel.management.service.AuditLogService;
import com.hostel.management.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final RoomAllocationRepository roomAllocationRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with email " + request.getEmail() + " already exists");
        }

        Role assignedRole = request.getRole() != null ? request.getRole() : Role.STUDENT;

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .role(assignedRole)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        String jwtToken = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        // 1. Password confirmation validation
        if (dto.getPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("Password and Confirm Password do not match.");
        }

        // 2. Email uniqueness validation
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with email " + email + " already exists.");
        }

        // 3. Phone uniqueness validation
        String phone = dto.getPhone() != null ? dto.getPhone().trim() : "";
        if (!phone.isBlank() && userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("An account with mobile number " + phone + " already exists.");
        }

        Role role = dto.getRole();
        if (role == null) {
            throw new BadRequestException("Please select a role to register.");
        }

        // 4. Role-specific validation & processing
        if (role == Role.STUDENT) {
            // Student Room & Bed Validation
            if (dto.getRoomNumber() == null || dto.getRoomNumber().isBlank()) {
                throw new BadRequestException("Room number is required for student registration.");
            }
            if (dto.getBedNumber() == null || dto.getBedNumber().isBlank()) {
                throw new BadRequestException("Bed number is required for student registration.");
            }

            Room room = roomRepository.findByRoomNumber(dto.getRoomNumber().trim())
                    .orElseThrow(() -> new BadRequestException("Selected room " + dto.getRoomNumber() + " does not exist."));

            if (room.getOccupied() >= room.getCapacity() || room.getStatus() == RoomStatus.FULL) {
                throw new BadRequestException("Selected room has no available bed.");
            }

            boolean bedTaken = roomAllocationRepository.existsByRoomIdAndBedNumberAndStatusIn(
                    room.getId(),
                    dto.getBedNumber().trim(),
                    List.of(AllocationStatus.ACTIVE, AllocationStatus.APPROVED)
            );
            if (bedTaken) {
                throw new BadRequestException("Bed " + dto.getBedNumber().trim() + " in room " + room.getRoomNumber() + " is already occupied.");
            }

            if (dto.getGender() == null) {
                throw new BadRequestException("Gender is required for student registration.");
            }
            if (dto.getGuardianName() == null || dto.getGuardianName().isBlank()) {
                throw new BadRequestException("Guardian Name is required.");
            }
            if (dto.getGuardianPhone() == null || dto.getGuardianPhone().isBlank()) {
                throw new BadRequestException("Guardian Mobile Number is required.");
            }
            if (dto.getEmergencyContact() == null || dto.getEmergencyContact().isBlank()) {
                throw new BadRequestException("Emergency Contact is required.");
            }
            if (dto.getPermanentAddress() == null || dto.getPermanentAddress().isBlank()) {
                throw new BadRequestException("Permanent Home Address is required.");
            }

            // Create User with BCrypt
            User user = User.builder()
                    .name(dto.getName().trim())
                    .email(email)
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .phone(phone)
                    .role(Role.STUDENT)
                    .enabled(true)
                    .build();
            User savedUser = userRepository.save(user);

            // Generate or sanitize admission number
            String admissionNum = dto.getAdmissionNumber() != null && !dto.getAdmissionNumber().isBlank()
                    ? dto.getAdmissionNumber().trim().toUpperCase()
                    : "STU-" + System.currentTimeMillis() % 100000;
            if (studentRepository.existsByAdmissionNumber(admissionNum)) {
                admissionNum = "STU-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            }

            Student student = Student.builder()
                    .user(savedUser)
                    .admissionNumber(admissionNum)
                    .college(dto.getCollege() != null ? dto.getCollege().trim() : null)
                    .course(dto.getCourse() != null && !dto.getCourse().isBlank() ? dto.getCourse().trim() : "General Studies")
                    .yearOfStudy(dto.getSemester() != null && !dto.getSemester().isBlank() ? dto.getSemester().trim() : "1st Year")
                    .gender(dto.getGender())
                    .dateOfBirth(dto.getDateOfBirth())
                    .hostelName(dto.getHostelName() != null && !dto.getHostelName().isBlank() ? dto.getHostelName().trim() : room.getBlockName())
                    .address(dto.getPermanentAddress().trim())
                    .guardianName(dto.getGuardianName().trim())
                    .guardianPhone(dto.getGuardianPhone().trim())
                    .emergencyContact(dto.getEmergencyContact().trim())
                    .build();
            Student savedStudent = studentRepository.save(student);

            // Create Active Room Allocation
            RoomAllocation allocation = RoomAllocation.builder()
                    .student(savedStudent)
                    .room(room)
                    .bedNumber(dto.getBedNumber().trim())
                    .status(AllocationStatus.ACTIVE)
                    .requestDate(LocalDate.now())
                    .startDate(LocalDate.now())
                    .remarks("Assigned upon self-registration")
                    .build();
            roomAllocationRepository.save(allocation);

            // Update room occupancy
            room.setOccupied(room.getOccupied() + 1);
            room.updateStatusBasedOnOccupancy();
            roomRepository.save(room);

            auditLogService.log(
                    savedUser.getId(),
                    savedUser.getName(),
                    "STUDENT",
                    "STUDENT_REGISTERED",
                    "AUTH",
                    String.valueOf(savedStudent.getId()),
                    "Registered as Student, assigned to Room " + room.getRoomNumber() + ", Bed " + dto.getBedNumber().trim()
            );

            log.info("Student registered: {} with Room {}, Bed {}", savedUser.getEmail(), room.getRoomNumber(), dto.getBedNumber().trim());
            return savedUser;

        } else if (role == Role.WARDEN) {
            User user = User.builder()
                    .name(dto.getName().trim())
                    .email(email)
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .phone(phone)
                    .role(Role.WARDEN)
                    .enabled(true)
                    .build();
            User savedUser = userRepository.save(user);

            Staff staff = Staff.builder()
                    .user(savedUser)
                    .employeeId(dto.getEmployeeId() != null ? dto.getEmployeeId().trim() : null)
                    .designation("Hostel Warden")
                    .hostelAssignment(dto.getHostelAssignment() != null ? dto.getHostelAssignment().trim() : "All Blocks")
                    .build();
            staffRepository.save(staff);

            auditLogService.log(
                    savedUser.getId(),
                    savedUser.getName(),
                    "WARDEN",
                    "WARDEN_REGISTERED",
                    "AUTH",
                    String.valueOf(savedUser.getId()),
                    "Registered as Warden with Employee ID " + dto.getEmployeeId()
            );

            log.info("Warden registered: {}", savedUser.getEmail());
            return savedUser;

        } else if (role == Role.ACCOUNTANT) {
            User user = User.builder()
                    .name(dto.getName().trim())
                    .email(email)
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .phone(phone)
                    .role(Role.ACCOUNTANT)
                    .enabled(true)
                    .build();
            User savedUser = userRepository.save(user);

            Staff staff = Staff.builder()
                    .user(savedUser)
                    .employeeId(dto.getEmployeeId() != null ? dto.getEmployeeId().trim() : null)
                    .designation("Hostel Accountant")
                    .build();
            staffRepository.save(staff);

            auditLogService.log(
                    savedUser.getId(),
                    savedUser.getName(),
                    "ACCOUNTANT",
                    "ACCOUNTANT_REGISTERED",
                    "AUTH",
                    String.valueOf(savedUser.getId()),
                    "Registered as Accountant with Employee ID " + dto.getEmployeeId()
            );

            log.info("Accountant registered: {}", savedUser.getEmail());
            return savedUser;

        } else if (role == Role.COMPLAINT_STAFF) {
            if (dto.getDepartment() == null) {
                throw new BadRequestException("Department is required for Complaint Department Staff registration (Electrical, Plumbing, Housekeeping, Internet / IT, Maintenance, Mess).");
            }

            User user = User.builder()
                    .name(dto.getName().trim())
                    .email(email)
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .phone(phone)
                    .role(Role.COMPLAINT_STAFF)
                    .enabled(true)
                    .build();
            User savedUser = userRepository.save(user);

            Staff staff = Staff.builder()
                    .user(savedUser)
                    .employeeId(dto.getEmployeeId() != null ? dto.getEmployeeId().trim() : null)
                    .department(dto.getDepartment())
                    .designation(dto.getDepartment().getDisplayName() + " Staff")
                    .build();
            staffRepository.save(staff);

            auditLogService.log(
                    savedUser.getId(),
                    savedUser.getName(),
                    "COMPLAINT_STAFF",
                    "STAFF_REGISTERED",
                    "AUTH",
                    String.valueOf(savedUser.getId()),
                    "Registered as Complaint Department Staff for " + dto.getDepartment().getDisplayName()
            );

            log.info("Complaint Staff registered: {} (Dept: {})", savedUser.getEmail(), dto.getDepartment());
            return savedUser;

        } else {
            throw new BadRequestException("Invalid role selected: " + role);
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!user.isEnabled()) {
            throw new BadRequestException("Your account is disabled. Please contact the hostel warden.");
        }

        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
