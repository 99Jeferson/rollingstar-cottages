package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.model.Cottage;
import com.rollingstar.cottages.repository.CottageRepository;
import com.rollingstar.cottages.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cottages")
public class CottageViewController {

    @Autowired
    private CottageRepository cottageRepository;

    @Autowired
    private BookingService bookingService;

    // 1. Render the main Grid Dashboard
    @GetMapping
    public String showCottagesDashboard(Model model) {
        model.addAttribute("cottages", cottageRepository.findAll());
        return "cottages";
    }

    // 2. Desk App Arrival: Capture Guest Name, Phone number, and National ID (NIN)
    @PostMapping("/check-in")
    public String processPhysicalCheckIn(
            @RequestParam("code") String code,
            @RequestParam("guestName") String guestName,
            @RequestParam("guestPhone") String guestPhone,
            @RequestParam("nin") String nin) {
        
        Cottage cottage = cottageRepository.findById(code).orElse(null);
        if (cottage != null) {
            // Save details to mark the cottage occupied
            cottage.setStatus("OCCUPIED");
            cottage.setGuest(guestName + " (NIN: " + nin + " | Mob: " + guestPhone + ")");
            cottageRepository.save(cottage);
            
            // Optional: Link directly to your booking management service ledger
            // bookingService.completePhysicalCheckIn(cottage.getActiveBookingId(), nin);
        }
        return "redirect:/cottages";
    }

    // 3. Desk App Departure: Process Terminal Ledger Settlement
    @PostMapping("/check-out")
    public String processPhysicalCheckOut(
            @RequestParam("code") String code,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "customerPhone", required = false) String customerPhone) {
        
        Cottage cottage = cottageRepository.findById(code).orElse(null);
        if (cottage != null) {
            // Clear out the room to make it ready for the next guest reservation
            cottage.setStatus("AVAILABLE");
            cottage.setGuest(null);
            cottageRepository.save(cottage);
        }
        return "redirect:/cottages";
    }

    // 4. Housekeeping Quick Toggle (Available <-> Maintenance)
    @PostMapping("/toggle-status")
    public String quickToggleStatus(@RequestParam("code") String code) {
        Cottage cottage = cottageRepository.findById(code).orElse(null);
        if (cottage != null) {
            if ("MAINTENANCE".equals(cottage.getStatus())) {
                cottage.setStatus("AVAILABLE");
            } else if ("AVAILABLE".equals(cottage.getStatus()) || cottage.getStatus() == null) {
                cottage.setStatus("MAINTENANCE");
            }
            cottageRepository.save(cottage);
        }
        return "redirect:/cottages";
    }
}