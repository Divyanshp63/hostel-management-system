package com.hostel.management.controller.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class CustomAppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exceptionObj = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object messageObj = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        if (statusObj != null) {
            try {
                statusCode = Integer.parseInt(statusObj.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        // Keep complete exception details in server logs for debugging
        if (exceptionObj instanceof Throwable throwable) {
            log.error("HTTP {} error on URI: {} - Exception: {}", statusCode, requestUri, throwable.getMessage(), throwable);
        } else {
            log.warn("HTTP {} error on URI: {} - Message: {}", statusCode, requestUri, messageObj);
        }

        model.addAttribute("errorCode", statusCode);

        if (statusCode == 404) {
            model.addAttribute("errorTitle", "Page Not Found");
            model.addAttribute("errorMessage", "The page you are looking for does not exist or has been moved.");
            return "error/404";
        } else if (statusCode == 403) {
            model.addAttribute("errorTitle", "Access Denied");
            model.addAttribute("errorMessage", "You do not have permission to access this page or resource.");
            return "error/access-denied";
        } else {
            model.addAttribute("errorTitle", "Something Went Wrong");
            model.addAttribute("errorMessage", "We couldn't load this page. Please try again.");
            return "error/500";
        }
    }
}
