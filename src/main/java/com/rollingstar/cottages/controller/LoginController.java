package com.rollingstar.cottages.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    /**
     * Renders the stylized system authentication login template page view interface.
     * Enhanced to capture security filters like errors, invalid logouts, or session expirations.
     */
    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            Model model) {
        
        // Pass dynamic alerts into your login.html template layout if flagged by Spring Security
        if (error != null) {
            model.addAttribute("loginError", "Invalid username or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been successfully logged out.");
        }
        if (expired != null) {
            model.addAttribute("expiredMessage", "Your session has expired or was terminated by management.");
        }

        return "login"; // Maps straight to src/main/resources/templates/login.html
    }

    /**
     * Safety fallback to automatically sweep incoming root web requests toward the login portal.
     */
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }
}