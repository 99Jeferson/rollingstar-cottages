package com.rollingstar.cottages.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cottages")
public class Cottage {

    @Id
    @Column(nullable = false, unique = true)
    private String code; // e.g., ST-01, DL-01, SU-01

    @Column(nullable = false)
    private String type; // Standard Cottage, Deluxe Double Room, VIP Executive Suite

    @Column(nullable = false)
    private double rate; // Nightly rate in UGX

    @Column(nullable = false)
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE

    @Column(name = "guests")
    private String guest; // Nullable if available

    // Default Constructor (Required by JPA)
    public Cottage() {}

    // Parameterized Constructor
    public Cottage(String code, String type, double rate, String status, String guest) {
        this.code = code;
        this.type = type;
        this.rate = rate;
        this.status = status;
        this.guest = guest;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGuest() { return guest; }
    public void setGuest(String guest) { this.guest = guest; }
}