package com.rollingstar.cottages.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "billing_tabs")
public class BillingTab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tab_name")
    private String tabName;
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "status")
    private String status = "OPEN"; // OPEN or SETTLED

    @OneToMany(mappedBy = "billingTab", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TabItem> items;

    // Audit Fields
    @Column(name = "settled_by")
    private String settledBy;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    // Default Constructor
    public BillingTab() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTabName() { return tabName; }
    public void setTabName(String tabName) { this.tabName = tabName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<TabItem> getItems() { return items; }
    public void setItems(List<TabItem> items) { this.items = items; }

    public String getSettledBy() { return settledBy; }
    public void setSettledBy(String settledBy) { this.settledBy = settledBy; }

    public LocalDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(LocalDateTime settledAt) { this.settledAt = settledAt; }
}