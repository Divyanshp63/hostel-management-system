package com.hostel.management.service.impl;

import com.hostel.management.dto.request.CreateStudentRequest;
import com.hostel.management.dto.request.UpdateStudentRequest;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.StudentResponse;
import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.enums.Role;
import com.hostel.management.exception.DuplicateResourceException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.exception.UnauthorizedException;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new DuplicateResourceException("Email is already registered: " + request.getEmail());
        }

        if (studentRepository.existsByAdmissionNumber(request.getAdmissionNumber().trim().toUpperCase())) {
            throw new DuplicateResourceException("Admission number already exists: " + request.getAdmissionNumber());
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone().trim())
                .role(Role.STUDENT)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        Student student = Student.builder()
                .user(savedUser)
                .admissionNumber(request.getAdmissionNumber().trim().toUpperCase())
                .course(request.getCourse().trim())
                .yearOfStudy(request.getYearOfStudy().trim())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .bloodGroup(request.getBloodGroup() != null ? request.getBloodGroup().trim().toUpperCase() : null)
                .address(request.getAddress().trim())
                .guardianName(request.getGuardianName().trim())
                .guardianPhone(request.getGuardianPhone().trim())
                .emergencyContact(request.getEmergencyContact().trim())
                .build();

        Student savedStudent = studentRepository.save(student);
        return mapToResponse(savedStudent);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> getAllStudents(int page, int size, String sortBy, String sortDir, String search) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Student> studentPage = studentRepository.searchStudents(search != null ? search.trim() : "", pageable);

        List<StudentResponse> responses = studentPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<StudentResponse>builder()
                .content(responses)
                .pageNumber(studentPage.getNumber())
                .pageSize(studentPage.getSize())
                .totalElements(studentPage.getTotalElements())
                .totalPages(studentPage.getTotalPages())
                .last(studentPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        return mapToResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentByCurrentUser(String currentUserEmail) {
        Student student = studentRepository.findByUserEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUserEmail));
        return mapToResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, UpdateStudentRequest request, String currentUserEmail) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        // Enforce ownership: STUDENT can only update their own profile; WARDEN can update any
        if (currentUser.getRole() == Role.STUDENT && !student.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update another student's profile");
        }

        User residentUser = student.getUser();
        residentUser.setName(request.getName().trim());
        residentUser.setPhone(request.getPhone().trim());
        userRepository.save(residentUser);

        student.setCourse(request.getCourse().trim());
        student.setYearOfStudy(request.getYearOfStudy().trim());
        student.setGender(request.getGender());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setBloodGroup(request.getBloodGroup() != null ? request.getBloodGroup().trim().toUpperCase() : null);
        student.setAddress(request.getAddress().trim());
        student.setGuardianName(request.getGuardianName().trim());
        student.setGuardianPhone(request.getGuardianPhone().trim());
        student.setEmergencyContact(request.getEmergencyContact().trim());

        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        User user = student.getUser();
        studentRepository.delete(student);
        userRepository.delete(user);
    }

    private StudentResponse mapToResponse(Student student) {
        User user = student.getUser();
        return StudentResponse.builder()
                .id(student.getId())
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .admissionNumber(student.getAdmissionNumber())
                .course(student.getCourse())
                .yearOfStudy(student.getYearOfStudy())
                .gender(student.getGender())
                .dateOfBirth(student.getDateOfBirth())
                .bloodGroup(student.getBloodGroup())
                .address(student.getAddress())
                .guardianName(student.getGuardianName())
                .guardianPhone(student.getGuardianPhone())
                .emergencyContact(student.getEmergencyContact())
                .enabled(user.isEnabled())
                .createdAt(student.getCreatedAt())
                .build();
    }
}
