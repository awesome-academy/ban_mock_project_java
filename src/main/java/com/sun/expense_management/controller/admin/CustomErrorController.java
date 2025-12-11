package com.sun.expense_management.controller.admin;

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
                    model.addAttribute("error", "Trang không tồn tại");
                    model.addAttribute("message", "Không tìm thấy trang bạn đang tìm kiếm.");
                    return "error/404";
                } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                    model.addAttribute("error", "Truy cập bị từ chối");
                    model.addAttribute("message", "Bạn không có quyền truy cập trang này.");
                    return "error/403";
                } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    model.addAttribute("error", "Lỗi hệ thống");
                    model.addAttribute("message", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
                    return "error/500";
                }

                // Generic error page for other status codes
                model.addAttribute("error", "Lỗi " + statusCode);
                model.addAttribute("message", "Đã xảy ra lỗi. Vui lòng thử lại sau.");
                return "error/error";
            }
        }

        // For API paths, return forward to default Spring Boot error handler (JSON)
        return "forward:/error-json";
    }
}
