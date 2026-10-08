package com.hostel.management.security;

import com.hostel.management.entity.User;
import com.hostel.management.enums.Role;
import com.hostel.management.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles post-login redirection based on the user's role and stores session attributes.
 * Flow:
 * WARDEN -> /warden/dashboard
 * ACCOUNTANT -> /accountant/dashboard
 * STUDENT -> /student/dashboard
 * COMPLAINT_STAFF -> /complaint-staff/dashboard
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        log.info("User logged in successfully: {}", email);

        HttpSession session = request.getSession(true);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            session.setAttribute("currentUser", user);
            session.setAttribute("userName", user.getName());
            session.setAttribute("userRole", user.getRole().name());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userId", user.getId());

            Role role = user.getRole();
            if (role == Role.WARDEN) {
                response.sendRedirect(request.getContextPath() + "/warden/dashboard");
                return;
            } else if (role == Role.ACCOUNTANT) {
                response.sendRedirect(request.getContextPath() + "/accountant/dashboard");
                return;
            } else if (role == Role.STUDENT) {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            } else if (role == Role.COMPLAINT_STAFF) {
                response.sendRedirect(request.getContextPath() + "/complaint-staff/dashboard");
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/");
    }
}
