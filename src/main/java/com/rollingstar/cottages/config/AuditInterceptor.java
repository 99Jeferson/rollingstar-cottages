package com.rollingstar.cottages.config;

import com.rollingstar.cottages.model.AuditLog;
import com.rollingstar.cottages.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditLogRepository auditLogRepository;

    public AuditInterceptor(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String route = request.getRequestURI();
        
        // Skip background long polling / asset noise logs
        if (route.contains("/css") || route.contains("/js") || route.contains("/error")) return;

        String method = request.getMethod();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) ? auth.getName() : "UNAUTHENTICATED";

        String actionDescription = "Accessed system via HTTP " + method;
        String executionStatus = "AUTHORIZED";

        if (response.getStatus() == 403 || response.getStatus() == 401) {
            actionDescription = "Unauthorized Access Attempt Blocked";
            executionStatus = "DENIED (" + response.getStatus() + ")";
        } else if (route.contains("/check-in") && "POST".equalsIgnoreCase(method)) {
            actionDescription = "Executed Live Front Desk Check-In Procedure";
        } else if (route.contains("/check-out") && "POST".equalsIgnoreCase(method)) {
            actionDescription = "Executed Client Checkout and Account Balancing";
        } else if (route.contains("/auditing")) {
            executionStatus = "ROOT ADMIN";
            actionDescription = "Elevated Security Terminal Monitored Session Initialization";
        }

        // Persist instantly to database footprint
        auditLogRepository.save(new AuditLog(username, actionDescription, route, executionStatus));
    }
}