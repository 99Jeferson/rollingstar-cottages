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
        // Drops old Hibernate session snapshots so it reads fresh database entries
        entityManager.clear();

        List<Cottage> cottageList = cottageRepository.findAll();
        LocalDate today = LocalDate.now();

        // Loop through all cottages to cross-reference active online website bookings
        for (Cottage cottage : cottageList) {
            String code = cottage.getCode(); 
            if (code == null || !code.contains("-")) continue;

            try {
                // 1. Translate dashboard cottage code format (e.g., "ST-03") into website naming convention
                String[] parts = code.split("-");
                String prefix = parts[0].trim().toUpperCase();  
                int roomNum = Integer.parseInt(parts[1].trim()); 

                String webRoomType = "";
                if ("ST".equals(prefix)) webRoomType = "standard";
                else if ("DL".equals(prefix)) webRoomType = "deluxe";
                else if ("SU".equals(prefix)) webRoomType = "suite";

                // 2. Scan the bookings table ignoring text casing traps
                List<Booking> activeWebBookings = bookingRepository.findByRoomTypeAndRoomNumberAndStatusIgnoreCase(webRoomType, roomNum, "confirmed");

                // Clear any leftover UI state before matching so stale loop reads don't compound
                cottage.setStatus("AVAILABLE");
                cottage.setGuest(null);

                for (Booking webBooking : activeWebBookings) {
                    LocalDate checkIn = webBooking.getCheckInDate(); 
                    LocalDate checkOut = webBooking.getCheckOutDate();

                    if (checkIn == null || checkOut == null) continue;

                    // 3. Dynamic Timeline Evaluation
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

    // FUNCTION 1: Toggle Room Status (Instant Maintenance/Available)
    @PostMapping("/cottages/toggle-status")
    public String toggleRoomStatus(@RequestParam("code") String code) {
        Optional<Cottage> optionalCottage = cottageRepository.findById(code);
        if (optionalCottage.isPresent()) {
            Cottage cottage = optionalCottage.get();
            if ("AVAILABLE".equalsIgnoreCase(cottage.getStatus())) {
                cottage.setStatus("MAINTENANCE");
                cottage.setGuest(null); // Clear guest text if moving to maintenance
            } else if ("MAINTENANCE".equalsIgnoreCase(cottage.getStatus())) {
                cottage.setStatus("AVAILABLE");
            }
            cottageRepository.save(cottage);
        }
        return "redirect:/cottages";
    }

    // FUNCTION 2: Manage Room (Assign Walk-In Guest & Check In)
    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/cottages/check-in")
    public String checkInGuest(@RequestParam("code") String code, @RequestParam("guestName") String guestName) {
        System.out.println("📥 FRONT DESK EVENT: Processing check-in for cottage: " + code + " | Guest: " + guestName);
        
        Optional<Cottage> optionalCottage = cottageRepository.findById(code);
        if (optionalCottage.isPresent() && guestName != null && !guestName.trim().isEmpty()) {
            Cottage cottage = optionalCottage.get();
            
            // Update local cottage layout state
            cottage.setGuest(guestName);
            cottage.setStatus("OCCUPIED");
            cottageRepository.save(cottage);
            System.out.println("💾 Local cottage table updated to OCCUPIED for " + code);

            try {
                String[] parts = code.split("-");
                String prefix = parts[0].trim().toUpperCase();  
                int roomNum = Integer.parseInt(parts[1].trim()); 

                String webRoomType = "";
                String defaultCapacity = "2"; // Default fallback capacity allocation

                if ("ST".equals(prefix)) {
                    webRoomType = "standard";
                    defaultCapacity = "2";
                } else if ("DL".equals(prefix)) {
                    webRoomType = "deluxe";
                    defaultCapacity = "3";
                } else if ("SU".equals(prefix)) {
                    webRoomType = "suite";
                    defaultCapacity = "4";
                }

                System.out.println("🚀 Entity Sync Input Data -> Type: '" + webRoomType + "' | Number: " + roomNum);

                // JPA Repository Approach to persist smoothly via entity structure
                Booking walkInBooking = new Booking();
                walkInBooking.setGuestName(guestName + " 🏢 [Desk Walk-In]");
                walkInBooking.setEmail("frontdesk@rollingstar.com");
                walkInBooking.setRoomType(webRoomType);
                walkInBooking.setRoomNumber(roomNum);
                walkInBooking.setStatus("confirmed");
                walkInBooking.setCheckInDate(LocalDate.now());
                walkInBooking.setCheckOutDate(LocalDate.now().plusDays(1));

                // ✨ FOOLPROOF FIX: Assign capacity string directly without using volatile cottage reflection fields
                walkInBooking.setGuests(defaultCapacity);

                bookingRepository.save(walkInBooking);
                System.out.println("✨ POSTGRES CONFIRMATION: Managed Booking Entity inserted via Repository!");

            } catch (Exception e) {
                System.err.println("❌ BACKEND ERROR: Booking sync failed: " + e.getMessage());
                throw e; 
            }
        } else {
            System.out.println("⚠️ Check-in skipped: Invalid input or cottage not found.");
        }
        return "redirect:/cottages";
    }

    // FUNCTION 3: Manage Room (Vacate Room & Check Out Walk-In)
    @org.springframework.transaction.annotation.Transactional 
    @PostMapping("/cottages/check-out")
    public String checkOutGuest(@RequestParam("code") String code) {
        System.out.println("📤 FRONT DESK EVENT: Attempting check-out for cottage: " + code);
        
        Optional<Cottage> optionalCottage = cottageRepository.findById(code);
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

                // Broaden the delete condition to drop records purely by coordinates and confirmation status.
                // This guarantees that mismatched desk fallback emails do not prevent checkout.
                int rowsDeleted = entityManager.createNativeQuery(
                    "DELETE FROM bookings WHERE LOWER(room_type) = LOWER(:roomType) AND room_number = :roomNumber AND LOWER(status) = 'confirmed'"
                )
                .setParameter("roomType", webRoomType)
                .setParameter("roomNumber", roomNum)
                .executeUpdate();

                System.out.println("🗑️ POSTGRES CONFIRMATION: Cleaned up blocks. Rows deleted: " + rowsDeleted);

            } catch (Exception e) {
                System.err.println("⚠️ Warning: Could not clear website table block: " + e.getMessage());
            }

            // Reset local cottage dashboard card layout cleanly
            cottage.setGuest(null);
            cottage.setStatus("AVAILABLE");
            cottageRepository.save(cottage);
            System.out.println("💾 Local cottage table reset to AVAILABLE for " + code);
        }
        return "redirect:/cottages";
    }
}