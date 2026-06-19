package com.rollingstar.cottages.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.rollingstar.cottages.model.User;
import com.rollingstar.cottages.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // 

    @Override
    public User registerNewUser(String username, String plainPassword, String role) {
        // Cryptographic Hashing: Secure the raw incoming password string
        String encryptedPassword = passwordEncoder.encode(plainPassword);
        
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(encryptedPassword); //
        newUser.setRole(role);
        
        return userRepository.save(newUser);
    }

    @Override
    public boolean verifyCredentials(String username, String plainPassword) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            // Securely evaluate match patterns against the database record hash
            return passwordEncoder.matches(plainPassword, user.getPasswordHash()); //  method
        }
        return false;
    }
}