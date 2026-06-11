package com.rollingstar.cottages.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "bookings") // Matches your website's database table name
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✨ FIX: Map directly to the actual column causing the constraint violation
    @Column(name = "guest_name", nullable = false) 
    private String guestName;

    // 🌟 ADDED: Keeps the 'guests' column mapped safely if the database or website uses it for count/details
    @Column(name = "guests")
    private String guests;

    @Column(nullable = false)
    private String email;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "room_type", nullable = false)
    private String roomType; // "standard", "deluxe", "suite"

    @Column(name = "room_number")
    private Integer roomNumber; // e.g., 1, 2, 3, 4...

    @Column(nullable = false)
    private String status; // "confirmed", "expired", "cancelled"

    // Default Constructor
    public Booking() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuests() { return guests; }
    public void setGuests(String guests) { this.guests = guests; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public Integer getRoomNumber() { return roomNumber; }
    public void setRoomNumber(Integer roomNumber) { this.roomNumber = roomNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}