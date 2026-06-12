package com.rollingstar.cottages.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_members")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 50)
    private String currentPosition; // e.g., Bartender, Receptionist, Security Guard

    @Column(nullable = false, length = 50)
    private String departmentAssignment; // e.g., Main Lounge Area, Cottage Hospitality

    @Column(nullable = false, length = 20)
    private String employmentStatus; // ACTIVE, ON_BREAK, TERMINATED, REPLACED

    @Column(length = 100)
    private String systemUserRole; // Associated role if they have a login account (e.g., ROLE_MANAGER)

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Constructors ---
    public Staff() {}

    public Staff(String fullName, String currentPosition, String departmentAssignment, String employmentStatus, String systemUserRole) {
        this.fullName = fullName;
        this.currentPosition = currentPosition;
        this.departmentAssignment = departmentAssignment;
        this.employmentStatus = employmentStatus;
        this.systemUserRole = systemUserRole;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(String currentPosition) { this.currentPosition = currentPosition; }

    public String getDepartmentAssignment() { return departmentAssignment; }
    public void setDepartmentAssignment(String departmentAssignment) { this.departmentAssignment = departmentAssignment; }

    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }

    public String getSystemUserRole() { return systemUserRole; }
    public void setSystemUserRole(String systemUserRole) { this.systemUserRole = systemUserRole; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}