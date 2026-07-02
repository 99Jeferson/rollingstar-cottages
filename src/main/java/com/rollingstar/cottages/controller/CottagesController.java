package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.model.Booking;
import com.rollingstar.cottages.model.Cottage;
import com.rollingstar.cottages.service.BookingService;
import com.rollingstar.cottages.repository.CottageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/system-cottages")
public class CottagesController {

    @Autowired 
    private CottageRepository cottageRepository;
    
    @Autowired 
    private BookingService bookingService;

    // Desktop UI Filter 1: Get rooms by type (e.g., /api/v1/system-cottages/filter/type?roomType=Deluxe)
    @GetMapping("/filter/type")
    public ResponseEntity<List<Cottage>> getCottagesByType(@RequestParam String roomType) {
        return ResponseEntity.ok(cottageRepository.findByTypeIgnoreCase(roomType));
    }

    // Desktop UI Filter 2: Get rooms under a budget threshold
    @GetMapping("/filter/price")
    public ResponseEntity<List<Cottage>> getCottagesByMaxPrice(@RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(cottageRepository.findByRateLessThanEqual(maxPrice));
    }

    // Web Endpoint: Initial online reservation processing
    @PostMapping("/reserve/online")
    public ResponseEntity<Booking> createOnlineCommitmentReservation(@RequestBody Booking booking) {
        return ResponseEntity.ok(bookingService.processOnlineCommitment(booking));
    }

    // Reception Desk Check-in: Capture National ID details
    @PatchMapping("/check-in/{bookingId}")
    public ResponseEntity<Booking> finalizeCheckInArrival(
            @PathVariable Long bookingId, 
            @RequestParam String nin) {
        return ResponseEntity.ok(bookingService.completePhysicalCheckIn(bookingId, nin));
    }

    // Billing Engine Endpoint: Issue printable receipt string text
    @GetMapping("/bookings/{bookingId}/receipt")
    public ResponseEntity<String> printReceipt(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.generateElectronicReceipt(bookingId));
    }
}