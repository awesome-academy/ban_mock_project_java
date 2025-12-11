package com.sun.expense_management.controller.admin;

import com.sun.expense_management.dto.admin.AdminLoginRequest;
import com.sun.expense_management.entity.User;
import com.sun.expense_management.repository.UserRepository;
import com.sun.expense_management.util.MessageUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for admin authentication
 */
@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final MessageUtil messageUtil;

    public AdminAuthController(AuthenticationManager authenticationManager,
                              UserRepository userRepository,
                              MessageUtil messageUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.messageUtil = messageUtil;
    }

    /**
     * Show admin login page
     */
    @GetMapping("/login")
    public String showLoginPage(HttpServletRequest request, Model model) {
        model.addAttribute("loginRequest", new AdminLoginRequest());

        // Save the original URL to redirect after login
        String referer = request.getParameter("redirect");
        if (referer == null) {
            referer = request.getHeader("Referer");
        }

        // Only save if it's an admin path (not login page itself)
        if (referer != null && referer.contains("/admin/") && !referer.contains("/admin/login")) {
            // Extract path from full URL
            try {
                java.net.URI uri = new java.net.URI(referer);
                String path = uri.getPath();
                if (uri.getQuery() != null) {
                    path += "?" + uri.getQuery();
                }
                request.getSession().setAttribute("REDIRECT_URL_AFTER_LOGIN", path);
            } catch (Exception e) {
                // Invalid URL, ignore
            }
        }

        return "admin/login";
    }

    /**
     * Handle admin login
     */
    @PostMapping("/login")
    public String processLogin(@Valid @ModelAttribute("loginRequest") AdminLoginRequest loginRequest,
                              BindingResult bindingResult,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        if (bindingResult.hasErrors()) {
            return "admin/login";
        }

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            // Verify ADMIN role
            User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

            if (user.getRole() != User.Role.ADMIN) {
                model.addAttribute("error", messageUtil.getMessage("admin.login.access.denied"));
                return "admin/login";
            }

            if (!user.getActive()) {
                model.addAttribute("error", messageUtil.getMessage("auth.account.inactive"));
                return "admin/login";
            }

            // Create security context
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            // Save to session
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            // Set session timeout if remember me
            if (Boolean.TRUE.equals(loginRequest.getRememberMe())) {
                session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 30 days
            }

            // Get saved redirect URL or default to dashboard
            String redirectUrl = (String) session.getAttribute("REDIRECT_URL_AFTER_LOGIN");
            if (redirectUrl != null) {
                session.removeAttribute("REDIRECT_URL_AFTER_LOGIN");
            } else {
                redirectUrl = "/admin/dashboard";
            }

            redirectAttributes.addFlashAttribute("success", messageUtil.getMessage("auth.login.success"));
            return "redirect:" + redirectUrl;

        } catch (BadCredentialsException e) {
            model.addAttribute("error", messageUtil.getMessage("auth.invalid.credentials"));
            return "admin/login";
        } catch (Exception e) {
            model.addAttribute("error", messageUtil.getMessage("error.internal.server"));
            return "admin/login";
        }
    }

    /**
     * Handle admin logout
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        redirectAttributes.addFlashAttribute("success", messageUtil.getMessage("admin.logout.success"));
        return "redirect:/admin/login";
    }
}
