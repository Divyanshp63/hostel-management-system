package com.hostel.management.controller.web;

import com.hostel.management.dto.request.UserRegistrationDto;
import com.hostel.management.entity.Room;
import com.hostel.management.entity.User;
import com.hostel.management.enums.ComplaintCategory;
import com.hostel.management.enums.Gender;
import com.hostel.management.enums.Role;
import com.hostel.management.enums.RoomStatus;
import com.hostel.management.repository.RoomRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebAuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @GetMapping("/")
    public String index() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                if (user.getRole() == Role.WARDEN) {
                    return "redirect:/warden/dashboard";
                } else if (user.getRole() == Role.ACCOUNTANT) {
                    return "redirect:/accountant/dashboard";
                } else if (user.getRole() == Role.STUDENT) {
                    return "redirect:/student/dashboard";
                } else if (user.getRole() == Role.COMPLAINT_STAFF) {
                    return "redirect:/complaint-staff/dashboard";
                }
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "denied", required = false) String denied,
            Model model
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Account registered successfully! Please log in.");
        }
        if (denied != null) {
            model.addAttribute("errorMessage", "Access Denied: You do not have permission to view that page.");
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }

        if (!model.containsAttribute("registration")) {
            model.addAttribute("registration", new UserRegistrationDto());
        }

        populateRegistrationFormModel(model);
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registration") UserRegistrationDto request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (request.getPassword() != null && request.getConfirmPassword() != null &&
                !request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            populateRegistrationFormModel(model);
            return "auth/register";
        }

        try {
            authService.registerUser(request);
            redirectAttributes.addFlashAttribute("successMessage", "Registration completed successfully! Please login with your credentials.");
            return "redirect:/login";
        } catch (Exception ex) {
            log.error("Registration error: {}", ex.getMessage());
            model.addAttribute("errorMessage", ex.getMessage());
            populateRegistrationFormModel(model);
            return "auth/register";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }

    private void populateRegistrationFormModel(Model model) {
        model.addAttribute("roles", Role.values());
        model.addAttribute("genders", Gender.values());
        model.addAttribute("departments", ComplaintCategory.values());
        List<Room> availableRooms = roomRepository.findByStatus(RoomStatus.AVAILABLE);
        model.addAttribute("availableRooms", availableRooms);
    }
}
