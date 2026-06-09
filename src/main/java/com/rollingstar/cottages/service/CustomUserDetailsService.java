package com.rollingstar.cottages.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Query your existing table structure
        String sql = "SELECT username, password, role FROM users WHERE username = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, username);

        if (rows.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        Map<String, Object> row = rows.get(0);
        String dbUsername = (String) row.get("username");
        String dbPassword = (String) row.get("password");
        String role = (String) row.get("role");

        // {noop} allows plain-text password comparison
        return User.withUsername(dbUsername)
                   .password("{noop}" + dbPassword)
                   .roles(role) // Spring automatically looks for ROLE_BARTENDER/MANAGER
                   .build();
    }
}