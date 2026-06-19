package com.rollingstar.cottages.service;

import com.rollingstar.cottages.model.User;
import com.rollingstar.cottages.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository; // 👈 Swapped out JdbcTemplate for type-safe JPA ORM

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Query cleanly via your repository layer
        User domainUser = userRepository.findByUsername(username);
        
        if (domainUser == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // Return the user details using the authentic BCrypt database hash string directly
        return org.springframework.security.core.userdetails.User.withUsername(domainUser.getUsername())
                   .password(domainUser.getPasswordHash()) // 👈 FIXED: No more "{noop}" prefix!
                   .roles(domainUser.getRole()) 
                   .build();
    }
}