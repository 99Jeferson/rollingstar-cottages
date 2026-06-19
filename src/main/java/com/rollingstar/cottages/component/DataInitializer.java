package com.rollingstar.cottages.component;

import com.rollingstar.cottages.model.User;
import com.rollingstar.cottages.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository; //

    @Autowired
    private PasswordEncoder passwordEncoder; 

    @Override
    public void run(String... args) throws Exception {
        // Safe object-relational count check
        if (userRepository.count() == 0) {
            System.out.println("--- Bootstrapping Master Root Account Into Empty DB ---");
            
            String username = "admin_boss";
            String rawPassword = "starboss2026";
            String role = "BOSS";
            
            // Cryptographically secure the password using BCrypt
            String encryptedPassword = passwordEncoder.encode(rawPassword);
            
            // Create and populate your clean User entity model
            User adminUser = new User();
            adminUser.setUsername(username);
            adminUser.setPasswordHash(encryptedPassword);
            adminUser.setRole(role);
            
            // Save cleanly via JPA ORM
            userRepository.save(adminUser);
            
            System.out.println("Root Account Created Safely with BCrypt Hash!");
        }
    }
}