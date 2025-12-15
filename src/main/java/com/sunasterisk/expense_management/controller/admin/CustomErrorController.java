package com.sunasterisk.expense_management.controller.admin;

import com.sunasterisk.expense_management.util.MessageUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom error controller to handle HTTP errors
 * Displays HTML error pages for admin panel instead of JSON
 */
@Controller
public class CustomErrorController implements ErrorController {

    private final MessageUtil messageUtil;

    public CustomErrorController(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            // If error is from admin path, show HTML error page
            if (path != null && path.toString().startsWith("/admin")) {
                model.addAttribute("status", statusCode);
                model.addAttribute("path", path);

                if (statusCode == HttpStatus.NOT_FOUND.value()) {
                    model.addAttribute("error", messageUtil.getMessage("error.404.title"));
                    model.addAttribute("message", messageUtil.getMessage("error.404.message"));
                    return "error/404";
                } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                    model.addAttribute("error", messageUtil.getMessage("error.403.title"));
                    model.addAttribute("message", messageUtil.getMessage("error.403.message"));
                    return "error/403";
                } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    model.addAttribute("error", messageUtil.getMessage("error.500.title"));
                    model.addAttribute("message", messageUtil.getMessage("error.500.message"));
                    return "error/500";
                }

                // Generic error page for other status codes
                model.addAttribute("error", messageUtil.getMessage("error.generic.title", statusCode));
                model.addAttribute("message", messageUtil.getMessage("error.generic.message"));
                return "error/error";
            }
        }

        // For API paths, return forward to default Spring Boot error handler (JSON)
        return "forward:/error-json";
    }
}
