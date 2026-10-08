package com.hostel.management.controller.web;

import com.hostel.management.entity.Staff;
import com.hostel.management.entity.User;
import com.hostel.management.enums.Role;
import com.hostel.management.repository.StaffRepository;
import com.hostel.management.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.hostel.management.controller.web")
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    @ModelAttribute("currentUserName")
    public String currentUserName(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userName") != null) {
            return (String) session.getAttribute("userName");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                if (session != null) {
                    session.setAttribute("userName", user.getName());
                }
                return user.getName();
            }
            return auth.getName();
        }
        return null;
    }

    @ModelAttribute("currentUserRole")
    public String currentUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userRole") != null) {
            return (String) session.getAttribute("userRole");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                if (session != null) {
                    session.setAttribute("userRole", user.getRole().name());
                }
                return user.getRole().name();
            }
        }
        return null;
    }

    @ModelAttribute("currentUserEmail")
    public String currentUserEmail(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userEmail") != null) {
            return (String) session.getAttribute("userEmail");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return auth.getName();
        }
        return null;
    }

    @ModelAttribute("currentUserDepartment")
    public String currentUserDepartment(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null && user.getRole() == Role.COMPLAINT_STAFF) {
                Staff staff = staffRepository.findByUser(user).orElse(null);
                if (staff != null && staff.getDepartment() != null) {
                    return staff.getDepartment().getDisplayName();
                }
            }
        }
        return null;
    }
}
