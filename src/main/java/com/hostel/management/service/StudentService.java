package com.hostel.management.service;

import com.hostel.management.dto.request.CreateStudentRequest;
import com.hostel.management.dto.request.UpdateStudentRequest;
import com.hostel.management.dto.response.PageResponse;
import com.hostel.management.dto.response.StudentResponse;

public interface StudentService {

    StudentResponse createStudent(CreateStudentRequest request);

    PageResponse<StudentResponse> getAllStudents(int page, int size, String sortBy, String sortDir, String search);

    StudentResponse getStudentById(Long id);

    StudentResponse getStudentByCurrentUser(String currentUserEmail);

    StudentResponse updateStudent(Long id, UpdateStudentRequest request, String currentUserEmail);

    void deleteStudent(Long id);
}
