package com.rollingstar.cottages.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.rollingstar.cottages.service.UserService;

@Controller
@RequestMapping("/admin/workers")
//  Only Allow BOSS and MANAGER roles to access any route within this entire controller
@PreAuthorize("hasAnyRole('BOSS', 'MANAGER')")
public class UserRegistrationController {

    @Autowired
    private UserService userService;

    /**
     * Displays the secure worker registration form page.
     */
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register-worker"; // Maps directly to register-worker.html in templates
    }

    /**
     * Processes the registration form, encrypts the password, and saves the new worker.
     */
    @PostMapping("/register")
    public String registerNewWorker(@RequestParam String username, 
                                    @RequestParam String password, 
                                    @RequestParam String role,
                                    Model model) {
        try {
            // Use our secure, injection-proof service layer to hash the temporary password
            userService.registerNewUser(username, password, role);
            
            model.addAttribute("successMsg", "Worker account successfully created for " + username + "!");
            return "register-worker";
            
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Failed to create account. Username might already be taken.");
            return "register-worker";
        }
    }
}