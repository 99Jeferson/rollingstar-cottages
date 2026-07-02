package com.rollingstar.cottages.service;

import com.rollingstar.cottages.model.User;
import com.rollingstar.cottages.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Query cleanly via your repository layer
        User domainUser = userRepository.findByUsername(username);
        
        if (domainUser == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // Build a direct authority mapping from the database role string
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(domainUser.getRole());

        // Return the user details using authentic BCrypt database hashes and strict authorities
        return org.springframework.security.core.userdetails.User.withUsername(domainUser.getUsername())
                   .password(domainUser.getPasswordHash()) 
                   .authorities(Collections.singleton(authority)) //  FIXED: Bypasses automatic "ROLE_" double-prefixing bugs
                   .build();
    }
}