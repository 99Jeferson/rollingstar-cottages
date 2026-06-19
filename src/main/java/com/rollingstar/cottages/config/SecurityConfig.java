package com.rollingstar.cottages.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig { 

    // Enables Thymeleaf to interpret "sec:authentication" and "sec:authorize" layout tags
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    /**
     * Cryptographic Encoder Component
     * Forces Spring Security to pass incoming login passwords through BCrypt 
     * before comparing them against database 'password_hash' records.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabled for development sandbox ease
            
            // 1. Define URL security matching access restrictions cleanly
            .authorizeHttpRequests(auth -> auth
                // Public access to assets and login
                .requestMatchers("/css/**", "/js/**", "/webjars/**", "/login").permitAll()
                
                // Boss-Only Auditing Portal
                .requestMatchers("/auditing/**").hasAnyAuthority("ROLE_BOSS", "BOSS")
                
                // Staffing Management Portal (Boss and Manager only)
                .requestMatchers("/staffing/**").hasAnyAuthority("ROLE_BOSS", "BOSS", "ROLE_MANAGER", "MANAGER")
                
                // Billing Engines (Manager, Boss, and Bartender)
                .requestMatchers("/billing/**").hasAnyAuthority("ROLE_BOSS", "BOSS", "ROLE_MANAGER", "MANAGER", "ROLE_BARTENDER", "BARTENDER")
                
                // Cottages Configurations (Manager and Boss only)
                .requestMatchers("/cottages/**").hasAnyAuthority("ROLE_BOSS", "BOSS", "ROLE_MANAGER", "MANAGER")
                
                // Global secure landing environments
                .requestMatchers("/dashboard").authenticated()
                .anyRequest().authenticated()
            )
            
            // 2. Configure Form Login parameters with explicit redirection mappings
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true) // Target landing zone upon successful verification
                .permitAll()
            )
            
            // 3. Handle active session invalidation limits for workers (e.g., locking out fired personnel)
            .sessionManagement(session -> session
                .maximumSessions(1) // Limits users to 1 concurrent logged-in session at a time
                .expiredUrl("/login?expired=true") // Redirects them out immediately if forced out by management
            )
            
            // 4. Configure unified secure system logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );
            
        return http.build();
    }
}