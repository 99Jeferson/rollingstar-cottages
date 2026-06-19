package com.rollingstar.cottages.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    /**
     * Renders the stylized system authentication login template page view interface.
     */
    @GetMapping("/login")
    public String showLoginPage() {
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