package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.model.Cottage;
import com.rollingstar.cottages.model.Booking;
import com.rollingstar.cottages.repository.CottageRepository;
import com.rollingstar.cottages.repository.BookingRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class CottagesController {

    private final CottageRepository cottageRepository;
    private final BookingRepository bookingRepository;
    private final EntityManager entityManager;

    public CottagesController(CottageRepository cottageRepository, BookingRepository bookingRepository, EntityManager entityManager) {
        this.cottageRepository = cottageRepository;
        this.bookingRepository = bookingRepository;
        this.entityManager = entityManager;
    }

    @GetMapping("/cottages")
    public String showCottagesDashboard(Model model) {
        entityManager.clear();

        List<Cottage> cottageList = cottageRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Cottage cottage : cottageList) {
            String code = cottage.getCode(); 
            if (code == null || !code.contains("-")) continue;

            try {
                String[] parts = code.split("-");
                String prefix = parts[0].trim().toUpperCase();  
                int roomNum = Integer.parseInt(parts[1].trim()); 

                String webRoomType = "";
                if ("ST".equals(prefix)) webRoomType = "standard";
                else if ("DL".equals(prefix)) webRoomType = "deluxe";
                else if ("SU".equals(prefix)) webRoomType = "suite";

                List<Booking> activeWebBookings = bookingRepository.findByRoomTypeAndRoomNumberAndStatusIgnoreCase(webRoomType, roomNum, "confirmed");

                // Set default base state
                cottage.setStatus("AVAILABLE"); 
                cottage.setGuest(null);

                for (Booking webBooking : activeWebBookings) {
                    LocalDate checkIn = webBooking.getCheckInDate(); 
                    LocalDate checkOut = webBooking.getCheckOutDate();

                    if (checkIn == null || checkOut == null) continue;

                    boolean isCurrentGuest = (!today.isBefore(checkIn)) && (!today.isAfter(checkOut));
                    boolean isUpcomingGuest = checkIn.isAfter(today);

                    if (isCurrentGuest) {
                        cottage.setStatus("OCCUPIED"); 
                        cottage.setGuest(webBooking.getGuestName() + " 🌐 [LIVE NOW]");
                        break; 
                    } 
                    else if (isUpcomingGuest) {
                        cottage.setStatus("OCCUPIED"); 
                        cottage.setGuest(webBooking.getGuestName() + " 🌐 [Arriving: " + checkIn + "]");
                        break; 
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error processing dashboard timeline parsing for room " + code + ": " + e.getMessage());
            }
        }

        model.addAttribute("cottages", cottageList);
        return "cottages";
    }

    @PostMapping("/cottages/toggle-status")
    public String toggleRoomStatus(@RequestParam("code") String code) {
        // 🌟 BYPASS: Using explicit findByCode to completely clear the findById error
        Optional<Cottage> optionalCottage = cottageRepository.findByCode(code);
        if (optionalCottage.isPresent()) {
            Cottage cottage = optionalCottage.get();
            
            String currentStatus = cottage.getStatus(); 
            
            if ("AVAILABLE".equalsIgnoreCase(currentStatus)) {
                cottage.setStatus("MAINTENANCE"); 
                cottage.setGuest(null);
            } else {
                cottage.setStatus("AVAILABLE"); 
            }
            cottageRepository.save(cottage);
        }
        return "redirect:/cottages";
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/cottages/check-in")
    public String checkInGuest(@RequestParam("code") String code, @RequestParam("guestName") String guestName) {
        System.out.println("📥 FRONT DESK EVENT: Processing check-in for cottage: " + code + " | Guest: " + guestName);
        
        Optional<Cottage> optionalCottage = cottageRepository.findByCode(code);
        if (optionalCottage.isPresent() && guestName != null && !guestName.trim().isEmpty()) {
            Cottage cottage = optionalCottage.get();
            
            cottage.setGuest(guestName);
            cottage.setStatus("OCCUPIED"); 
            cottageRepository.save(cottage);

            try {
                String[] parts = code.split("-");
                String prefix = parts[0].trim().toUpperCase();  
                int roomNum = Integer.parseInt(parts[1].trim()); 

                String webRoomType = "";
                int defaultCapacity = 2; 

                if ("ST".equals(prefix)) {
                    webRoomType = "standard";
                    defaultCapacity = 2;
                } else if ("DL".equals(prefix)) {
                    webRoomType = "deluxe";
                    defaultCapacity = 3;
                } else if ("SU".equals(prefix)) {
                    webRoomType = "suite";
                    defaultCapacity = 4;
                }

                Booking walkInBooking = new Booking();
                walkInBooking.setGuestName(guestName + " 🏢 [Desk Walk-In]");
                walkInBooking.setEmail("frontdesk@rollingstar.com");
                walkInBooking.setRoomType(webRoomType);
                walkInBooking.setRoomNumber(roomNum);
                walkInBooking.setStatus("confirmed");
                walkInBooking.setCheckInDate(LocalDate.now());
                walkInBooking.setCheckOutDate(LocalDate.now().plusDays(1));

                walkInBooking.setGuests(String.valueOf(defaultCapacity)); 

                bookingRepository.save(walkInBooking);
                System.out.println("✨ POSTGRES CONFIRMATION: Managed Booking Entity inserted via Repository!");

            } catch (Exception e) {
                System.err.println("❌ BACKEND ERROR: Booking sync failed: " + e.getMessage());
                throw e; 
            }
        }
        return "redirect:/cottages";
    }

    @org.springframework.transaction.annotation.Transactional 
    @PostMapping("/cottages/check-out")
    public String checkOutGuest(@RequestParam("code") String code) {
        System.out.println("📤 FRONT DESK EVENT: Attempting check-out for cottage: " + code);
        
        Optional<Cottage> optionalCottage = cottageRepository.findByCode(code);
        if (optionalCottage.isPresent()) {
            Cottage cottage = optionalCottage.get();
            
            try {
                String[] parts = code.split("-");
                String prefix = parts[0].trim().toUpperCase();  
                int roomNum = Integer.parseInt(parts[1].trim()); 

                String webRoomType = "";
                if ("ST".equals(prefix)) webRoomType = "standard";
                else if ("DL".equals(prefix)) webRoomType = "deluxe";
                else if ("SU".equals(prefix)) webRoomType = "suite";

                bookingRepository.deleteByRoomTypeIgnoreCaseAndRoomNumberAndStatusIgnoreCase(webRoomType, roomNum, "confirmed");
                System.out.println("🗑️ POSTGRES CONFIRMATION: Cleaned up blocks via safely derived repository invocation.");

            } catch (Exception e) {
                System.err.println("⚠️ Warning: Could not clear website table block: " + e.getMessage());
            }

            cottage.setGuest(null);
            cottage.setStatus("AVAILABLE"); 
            cottageRepository.save(cottage);
        }
        return "redirect:/cottages";
    }
}