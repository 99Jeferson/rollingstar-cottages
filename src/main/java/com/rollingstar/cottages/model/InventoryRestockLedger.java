package com.rollingstar.cottages.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_restock_ledger")
public class InventoryRestockLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private InventoryItem item;

    @Column(name = "quantity_added", nullable = false)
    private Integer quantityAdded;

    @Column(name = "cost_price", nullable = false)
    private BigDecimal costPrice;

    @Column(name = "restocked_by", nullable = false)
    private String restockedBy;

    @Column(name = "restocked_at")
    private LocalDateTime restockedAt = LocalDateTime.now();

    // Constructors
    public InventoryRestockLedger() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public InventoryItem getItem() { return item; }
    public void setItem(InventoryItem item) { this.item = item; }

    public Integer getQuantityAdded() { return quantityAdded; }
    public void setQuantityAdded(Integer quantityAdded) { this.quantityAdded = quantityAdded; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public String getRestockedBy() { return restockedBy; }
    public void setRestockedBy(String restockedBy) { this.restockedBy = restockedBy; }

    public LocalDateTime getRestockedAt() { return restockedAt; }
    public void setRestockedAt(LocalDateTime restockedAt) { this.restockedAt = restockedAt; }
}