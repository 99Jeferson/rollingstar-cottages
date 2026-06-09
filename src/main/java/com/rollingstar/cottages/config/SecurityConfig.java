package com.rollingstar.cottages.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Enables Thymeleaf to understand "sec:authentication" and "sec:authorize" tags
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public access to assets and login
                .requestMatchers("/css/**", "/js/**", "/webjars/**", "/login").permitAll()
                
                // 1. Boss-Only Auditing Portal (Accepts exact literal strings with or without prefixes)
                .requestMatchers("/auditing/**").hasAnyAuthority("ROLE_BOSS", "BOSS")
                
                // 2. Billing Engines (Manager, Boss, and Bartender)
                .requestMatchers("/billing/**").hasAnyAuthority("ROLE_BOSS", "BOSS", "ROLE_MANAGER", "MANAGER", "ROLE_BARTENDER", "BARTENDER")
                
                // 3. Cottages Configurations (Manager and Boss only)
                // 🌟 FIX: Using hasAnyAuthority matches your database roles EXACTLY as they are written!
                .requestMatchers("/cottages/**").hasAnyAuthority("ROLE_BOSS", "BOSS", "ROLE_MANAGER", "MANAGER")
                
                // 4. Global secure landing environments
                .requestMatchers("/dashboard").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );
            
        return http.build();
    }
}