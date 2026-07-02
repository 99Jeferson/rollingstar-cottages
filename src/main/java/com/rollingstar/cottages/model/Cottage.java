package com.rollingstar.cottages.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cottages")
public class Cottage {

    @Id
    @Column(nullable = false, unique = true, length = 255)
    private String code; // e.g., ST-01, DL-01, SU-01

    @Column(nullable = false)
    private String type; // Standard Cottage, Deluxe Double Room, VIP Executive Suite

    @Column(nullable = false)
    private BigDecimal rate; // Nightly rate in UGX

    @Column(nullable = false)
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE, PENDING_PAYMENT

    @Column(name = "guests")
    private String guest; // Nullable if available

    // FIXED: Changed reference type from StaffMember to your exact Staff entity class name
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private Staff assignedStaff; 

    // Default Constructor (Required by JPA)
    public Cottage() {}

    // Parameterized Constructor (FIXED: Updated parameter types to Staff)
    public Cottage(String code, String type, BigDecimal rate, String status, String guest, Staff assignedStaff) {
        this.code = code;
        this.type = type;
        this.rate = rate;
        this.status = status;
        this.guest = guest;
        this.assignedStaff = assignedStaff;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGuest() { return guest; }
    public void setGuest(String guest) { this.guest = guest; }

    // FIXED: Updated getters/setters types to match your Staff class
    public Staff getAssignedStaff() { return assignedStaff; }
    public void setAssignedStaff(Staff assignedStaff) { this.assignedStaff = assignedStaff; }
}