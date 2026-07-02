package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.model.PaymentTransaction;
import com.rollingstar.cottages.model.BillingTab;
import com.rollingstar.cottages.model.Cottage;
import com.rollingstar.cottages.service.PaymentService;
import com.rollingstar.cottages.repository.BillingRepository;
import com.rollingstar.cottages.repository.BookingRepository;
import com.rollingstar.cottages.repository.CottageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional; // Import for data integrity
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentWebhookController {

    private final PaymentService paymentService;
    private final BillingRepository billingRepository;
    private final BookingRepository bookingRepository;
    private final CottageRepository cottageRepository; 

    @Autowired
    public PaymentWebhookController(PaymentService paymentService,
                                    BillingRepository billingRepository,
                                    BookingRepository bookingRepository,
                                    CottageRepository cottageRepository) {
        this.paymentService = paymentService;
        this.billingRepository = billingRepository;
        this.bookingRepository = bookingRepository;
        this.cottageRepository = cottageRepository;
    }

    /**
     * Listens for asynchronous transaction status updates sent by the payment aggregator (MTN MoMo / Airtel Money).
     * Path: POST http://localhost:8080/api/v1/payments/webhook
     */
    @SuppressWarnings("unchecked")
    @Transactional //  Ensures all multi-table database releases succeed together or roll back cleanly
    @PostMapping("/webhook")
    public ResponseEntity<String> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        try {
            if (!payload.containsKey("event") || !payload.containsKey("data")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload structure");
            }

            String eventType = (String) payload.get("event");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");

            String txReference = (String) data.get("tx_reference"); // Cottages: "RS-PAY-ST-02-...", Bar: "RS-BAR-TAB-5-..."
            String externalId = (String) data.get("transaction_id");
            String gatewayStatus = (String) data.get("status");

            String finalSystemStatus = "FAILED";
            boolean isSuccessful = "payment.success".equalsIgnoreCase(eventType) || "SUCCESSFUL".equalsIgnoreCase(gatewayStatus);
            
            if (isSuccessful) {
                finalSystemStatus = "SUCCESSFUL";
            }

            // 1. Trigger the central transaction log update
            Optional<PaymentTransaction> updatedTx = paymentService.updateTransactionStatus(txReference, finalSystemStatus, externalId);

            if (updatedTx.isPresent()) {
                PaymentTransaction transaction = updatedTx.get();

                // 2. DOMAIN AUTOMATION SIDE-EFFECTS LOOP (Only executes on settled funds)
                if (isSuccessful) {
                    
                    // --- SCENARIO A: RESOLVE LOUNGE BAR TABS ---
                    // Explicitly handles the structured "RS-BAR-TAB-[Id]-[Timestamp]" reference pattern
                    if (txReference != null && txReference.startsWith("RS-BAR-")) {
                        try {
                            String[] refParts = txReference.split("-");
                            if (refParts.length >= 4) {
                                Long tabId = Long.parseLong(refParts[3]); // Extracts target ID from index position [3]
                                
                                Optional<BillingTab> optionalTab = billingRepository.findById(tabId);
                                optionalTab.ifPresent(tab -> {
                                    tab.setStatus("SETTLED");
                                    tab.setSettledAt(LocalDateTime.now());
                                    tab.setSettledBy("AUTOMATED_WEBHOOK_BOT");
                                    billingRepository.save(tab);
                                    System.out.println(" WEBHOOK AUTOMATION: Resolved Lounge Bar Tab ID " + tabId + " strictly via Transaction Reference.");
                                });
                            }
                        } catch (Exception e) {
                            System.err.println("⚠ WEBHOOK WARNING: Failed strict bar tab reference matching logic: " + e.getMessage());
                        }
                    } else {
                        // Legacy support fallback: Resolves PENDING_PAYMENT items based on matching exact values
                        billingRepository.findByStatus("PENDING_PAYMENT").stream()
                            .filter(tab -> tab.getTotalAmount().compareTo(transaction.getAmount()) == 0)
                            .findFirst()
                            .ifPresent(tab -> {
                                tab.setStatus("SETTLED");
                                tab.setSettledAt(LocalDateTime.now());
                                tab.setSettledBy("AUTOMATED_WEBHOOK_BOT");
                                billingRepository.save(tab);
                                System.out.println(" WEBHOOK AUTOMATION: Resolved Lounge Bar Tab ID " + tab.getId() + " via amount matching fallback.");
                            });
                    }

                    // --- SCENARIO B: RELEASE COTTAGE ROOMS ON CHECKOUT ---
                    // Parses cottage room code directly out of the incoming custom reference pattern
                    if (txReference != null && txReference.startsWith("RS-PAY-")) {
                        try {
                            String[] refParts = txReference.split("-");
                            if (refParts.length >= 4) {
                                String prefix = refParts[2].trim().toUpperCase();  // "ST", "DL", "SU"
                                String roomNumStr = refParts[3].trim();            // "02", "01"
                                String cottageCode = prefix + "-" + roomNumStr;    // Reconstructs "ST-02"

                                Optional<Cottage> optionalCottage = cottageRepository.findByCode(cottageCode);
                                if (optionalCottage.isPresent()) {
                                    Cottage cottage = optionalCottage.get();
                                    int roomNum = Integer.parseInt(roomNumStr); 

                                    String webRoomType = "";
                                    if ("ST".equals(prefix)) webRoomType = "standard";
                                    else if ("DL".equals(prefix)) webRoomType = "deluxe";
                                    else if ("SU".equals(prefix)) webRoomType = "suite";

                                    // 1. Drop the active web reservation block out of Postgres
                                    if (!webRoomType.isEmpty()) {
                                        bookingRepository.deleteByRoomTypeIgnoreCaseAndRoomNumberAndStatusIgnoreCase(webRoomType, roomNum, "confirmed");
                                        System.out.println(" WEBHOOK AUTOMATION: Cleaned up reservation blocks for " + cottageCode);
                                    }

                                    // 2. Clear out room guest data and flip status back to AVAILABLE live
                                    cottage.setGuest(null);
                                    cottage.setStatus("AVAILABLE");
                                    cottageRepository.save(cottage);
                                    System.out.println(" WEBHOOK AUTOMATION: Cottage " + cottageCode + " is now AVAILABLE.");
                                }
                            }
                        } catch (Exception e) {
                            System.err.println(" WEBHOOK WARNING: Could not auto-release cottage from tx_ref pattern: " + e.getMessage());
                        }
                    }

                    // --- SCENARIO C: CUSTOMER TELEMETRY ---
                    if (transaction.getCustomerPhone() != null) {
                        System.out.println(" WEBHOOK AUTOMATION: Processing payment log verification for customer handset: " + transaction.getCustomerPhone());
                    }
                }

                System.out.println(">>> Webhook processed successfully for Reference: " + txReference + " | Status: " + finalSystemStatus);
                return ResponseEntity.ok("Webhook acknowledged successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction reference not found in database");
            }

        } catch (Exception e) {
            System.err.println(">>> Error processing payment webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal processing error");
        }
    }
}