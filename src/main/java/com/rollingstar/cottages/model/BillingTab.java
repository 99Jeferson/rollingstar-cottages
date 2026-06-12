package com.rollingstar.cottages.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_tabs")
public class BillingTab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String referenceId; // Handled by line 62: newTab.setReferenceId(...)

    @Column(nullable = false)
    private String tabName; // Handled by line 41 & 60: BillingTab::getTotalAmount / newTab.setTabName(...)

    @Column(nullable = false)
    private BigDecimal totalAmount; // Handled by lines 41, 62, 119, 120

    @Column(nullable = false)
    private String departmentSource; // Handled by line 63: newTab.setDepartmentSource(...)

    @Column(nullable = false)
    private String status; // Handled by lines 34, 35, 61, 75: "OPEN", "SETTLED"

    private String settledBy; // Handled by line 76: tab.setSettledBy(...)
    private LocalDateTime settledAt; // Handled by line 77: tab.setSettledAt(...)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "OPEN";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Constructor ---
    public BillingTab() {}

    // --- Getters & Setters (Must match BillingController exactly) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getTabName() { return tabName; }
    public void setTabName(String tabName) { this.tabName = tabName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getDepartmentSource() { return departmentSource; }
    public void setDepartmentSource(String departmentSource) { this.departmentSource = departmentSource; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSettledBy() { return settledBy; }
    public void setSettledBy(String settledBy) { this.settledBy = settledBy; }

    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}