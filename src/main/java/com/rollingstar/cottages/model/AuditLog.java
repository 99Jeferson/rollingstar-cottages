package com.rollingstar.cottages.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String userPrincipal;
    private String actionEvent;
    private String resourceRoute;
    private String status;

    public AuditLog() {}

    public AuditLog(String userPrincipal, String actionEvent, String resourceRoute, String status) {
        this.timestamp = LocalDateTime.now();
        this.userPrincipal = userPrincipal;
        this.actionEvent = actionEvent;
        this.resourceRoute = resourceRoute;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getUserPrincipal() { return userPrincipal; }
    public void setUserPrincipal(String userPrincipal) { this.userPrincipal = userPrincipal; }
    public String getActionEvent() { return actionEvent; }
    public void setActionEvent(String actionEvent) { this.actionEvent = actionEvent; }
    public String getResourceRoute() { return resourceRoute; }
    public void setResourceRoute(String resourceRoute) { this.resourceRoute = resourceRoute; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}