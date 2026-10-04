package com.hostel.management.service;

import com.hostel.management.dto.request.LoginRequest;
import com.hostel.management.dto.request.RegisterRequest;
import com.hostel.management.dto.response.AuthResponse;
import com.hostel.management.dto.response.UserProfileResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserProfileResponse getCurrentUserProfile(String currentUserEmail);
}
