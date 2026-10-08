package com.hostel.management.service;

import com.hostel.management.dto.request.LoginRequest;
import com.hostel.management.dto.request.RegisterRequest;
import com.hostel.management.dto.request.UserRegistrationDto;
import com.hostel.management.dto.response.AuthResponse;
import com.hostel.management.dto.response.UserProfileResponse;
import com.hostel.management.entity.User;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    User registerUser(UserRegistrationDto dto);

    AuthResponse login(LoginRequest request);

    UserProfileResponse getCurrentUserProfile(String currentUserEmail);
}
