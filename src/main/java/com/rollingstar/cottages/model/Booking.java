package com.rollingstar.cottages.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Column(nullable = false)
    private String email;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "room_type", nullable = false)
    private String roomType; // Maps to the room code/type string (e.g., "ST-01", "standard")

    @Column(name = "room_number")
    private int roomNumber;

    @Column(nullable = false)
    private String status; // e.g., "confirmed", "OCCUPIED", "pending"

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOut;

    private String guests; // Capacity block metric

    @Column(name = "national_id_nin")
    private String nationalIdNin;

    @Column(name = "receipt_issued")
    private boolean receiptIssued;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "commitment_fee_paid")
    private BigDecimal commitmentFeePaid = BigDecimal.ZERO;

    @Column(name = "balance_due")
    private BigDecimal balanceDue = BigDecimal.ZERO;

    // Default Constructor (Required by JPA)
    public Booking() {}

    /**
     * Business Logic Hook: Computes balance metrics dynamically 
     * before persisting or returning financial details.
     */
    public void compileFinancialBalances() {
        if (this.totalAmount != null) {
            BigDecimal paid = (this.commitmentFeePaid != null) ? this.commitmentFeePaid : BigDecimal.ZERO;
            this.balanceDue = this.totalAmount.subtract(paid);
        }
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }

    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }

    public String getGuests() { return guests; }
    public void setGuests(String guests) { this.guests = guests; }

    public String getNationalIdNin() { return nationalIdNin; }
    public void setNationalIdNin(String nationalIdNin) { this.nationalIdNin = nationalIdNin; }

    public boolean isReceiptIssued() { return receiptIssued; }
    public void setReceiptIssued(boolean receiptIssued) { this.receiptIssued = receiptIssued; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getCommitmentFeePaid() { return commitmentFeePaid; }
    public void setCommitmentFeePaid(BigDecimal commitmentFeePaid) { this.commitmentFeePaid = commitmentFeePaid; }

    public BigDecimal getBalanceDue() { return balanceDue; }
    public void setBalanceDue(BigDecimal balanceDue) { this.balanceDue = balanceDue; }
}