package com.rollingstar.cottages.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    /**
     * Simply returns the login view. 
     * Spring Security handles the actual POST /login request automatically.
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; 
    }

    /**
     * Redirects the root URL to the login page.
     */
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }
}