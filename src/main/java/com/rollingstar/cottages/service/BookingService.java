package com.rollingstar.cottages.service;

import com.rollingstar.cottages.model.Booking;
import com.rollingstar.cottages.model.Cottage;
import com.rollingstar.cottages.repository.BookingRepository;
import com.rollingstar.cottages.repository.CottageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired 
    private BookingRepository bookingRepository;
    
    @Autowired 
    private CottageRepository cottageRepository;

    /**
     * LOGIC A: Handles the initial online booking phase from the website
     */
    @Transactional
    public Booking processOnlineCommitment(Booking webBooking) {
        // Looks up using the String room type/code field mapping
        Cottage cottage = cottageRepository.findById(webBooking.getRoomType())
                .orElseThrow(() -> new IllegalArgumentException("Target cottage code not found"));

        // Enforce state transitions
        cottage.setStatus("PARTIAL_BOOKING");
        cottageRepository.save(cottage);

        // Uses your model calculation method hook
        webBooking.compileFinancialBalances();
        Booking savedBooking = bookingRepository.save(webBooking);

        // Simulation Loop: Dispatches confirmation alert directly to terminal console log
        sendSmsNotification(savedBooking.getCustomerPhone(), 
            "Hello " + savedBooking.getGuestName() + ", your commitment payment of UGX " + 
            savedBooking.getCommitmentFeePaid() + " is verified. Booking Status: PARTIAL_BOOKING. Balance due on arrival.");

        return savedBooking;
    }

    /**
     * LOGIC B: Handles physical desk check-in, collects National ID details, and updates states
     */
    @Transactional
    public Booking completePhysicalCheckIn(Long bookingId, String nin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid booking reference code"));

        if (nin == null || nin.trim().length() < 10) {
            throw new IllegalArgumentException("Valid National Identification Number (NIN) is mandatory for arrival logs.");
        }

        booking.setNationalIdNin(nin.toUpperCase().trim());
        booking.setStatus("OCCUPIED");
        
        // Sync the room status to OCCUPIED if the code matches
        Optional<Cottage> cottageOpt = cottageRepository.findByCode(booking.getRoomType());
        if (cottageOpt.isPresent()) {
            Cottage cottage = cottageOpt.get();
            cottage.setStatus("OCCUPIED");
            cottage.setGuest(booking.getGuestName());
            cottageRepository.save(cottage);
        }
        
        return bookingRepository.save(booking);
    }

    /**
     * LOGIC C: Generates structured printable receipts following final clearance transactions
     */
    public String generateElectronicReceipt(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking reference not found"));

        booking.setReceiptIssued(true);
        bookingRepository.save(booking);

        Optional<Cottage> cottageOpt = cottageRepository.findByCode(booking.getRoomType());
        String cottageType = cottageOpt.isPresent() ? cottageOpt.get().getType() : booking.getRoomType();

        return "==================================================\n" +
               "           ROLLING STARS COTTAGES & LOUNGE       \n" +
               "==================================================\n" +
               " RECEIPT ID     : REC-00" + booking.getId() + "\n" +
               " GUEST NAME     : " + booking.getGuestName() + "\n" +
               " NATIONAL NIN   : " + (booking.getNationalIdNin() != null ? booking.getNationalIdNin() : "N/A") + "\n" +
               " ASSIGNED ROOM  : " + booking.getRoomType() + " (" + cottageType + ")\n" +
               "--------------------------------------------------\n" +
               " TOTAL BILL     : UGX " + booking.getTotalAmount() + "\n" +
               " ADVANCE PAID   : UGX " + booking.getCommitmentFeePaid() + "\n" +
               " FINAL CLEARANCE: UGX " + booking.getBalanceDue() + "\n" +
               " SETTLEMENT STATUS: FULLY PAID / CLOSED           \n" +
               "==================================================\n" +
               " Attendant Assigned: System Desk Receptionist\n" +
               " Thank you for choosing Rolling Stars! \n";
    }

    private void sendSmsNotification(String phoneNumber, String messageContent) {
        System.out.println("[SMS INFRASTRUCTURE OUTBOX] Targeting -> " + phoneNumber);
        System.out.println("[SMS CONTENT] " + messageContent);
    }
}