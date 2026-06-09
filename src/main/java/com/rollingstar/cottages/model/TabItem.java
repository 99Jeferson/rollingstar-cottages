package com.rollingstar.cottages.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tab_items")
public class TabItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tab_id")
    private BillingTab billingTab;

    @Column(name = "item_id")
    private Long itemId;
    
    @Column(name = "item_name")
    private String itemName;
    
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;

    // --- HELPER METHOD FOR AUTOMATIC MATH ---
    /**
     * Automatically recalculates the subtotal based on current price and quantity.
     */
    public void updateSubtotal() {
        if (this.price != null && this.quantity != null) {
            this.subtotal = this.price.multiply(BigDecimal.valueOf(this.quantity));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }

    // --- GETTERS AND SETTERS ---
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public BillingTab getBillingTab() { 
        return billingTab; 
    }
    
    public void setBillingTab(BillingTab billingTab) { 
        this.billingTab = billingTab; 
    }
    
    public Long getItemId() { 
        return itemId; 
    }
    
    public void setItemId(Long itemId) { 
        this.itemId = itemId; 
    }
    
    public String getItemName() { 
        return itemName; 
    }
    
    public void setItemName(String itemName) { 
        this.itemName = itemName; 
    }
    
    public BigDecimal getPrice() { 
        return price; 
    }
    
    public void setPrice(BigDecimal price) { 
        this.price = price;
        updateSubtotal(); // Automatically sync subtotal if price changes
    }
    
    public Integer getQuantity() { 
        return quantity; 
    }
    
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity; 
        updateSubtotal(); // Automatically sync subtotal if quantity changes
    }
    
    public BigDecimal getSubtotal() { 
        return subtotal; 
    }
    
    public void setSubtotal(BigDecimal subtotal) { 
        this.subtotal = subtotal; 
    }
}