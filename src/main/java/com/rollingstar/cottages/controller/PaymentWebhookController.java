package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.model.PaymentTransaction;
import com.rollingstar.cottages.model.BillingTab;
import com.rollingstar.cottages.model.Booking;
import com.rollingstar.cottages.service.PaymentService;
import com.rollingstar.cottages.repository.BillingRepository;
import com.rollingstar.cottages.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentWebhookController {

    private final PaymentService paymentService;
    private final BillingRepository billingRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public PaymentWebhookController(PaymentService paymentService,
                                    BillingRepository billingRepository,
                                    BookingRepository bookingRepository) {
        this.paymentService = paymentService;
        this.billingRepository = billingRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Listens for asynchronous transaction status updates sent by the payment aggregator.
     * Path: POST http://localhost:8080/api/v1/payments/webhook
     */
    @SuppressWarnings("unchecked")
    @PostMapping("/webhook")
    public ResponseEntity<String> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        try {
            if (!payload.containsKey("event") || !payload.containsKey("data")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload structure");
            }

            String eventType = (String) payload.get("event");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");

            String txReference = (String) data.get("tx_reference");
            String externalId = (String) data.get("transaction_id");
            String gatewayStatus = (String) data.get("status");

            String finalSystemStatus = "FAILED";
            boolean isSuccessful = "payment.success".equalsIgnoreCase(eventType) || "SUCCESSFUL".equalsIgnoreCase(gatewayStatus);
            
            if (isSuccessful) {
                finalSystemStatus = "SUCCESSFUL";
            }

            // 1. Trigger the transaction log update
            Optional<PaymentTransaction> updatedTx = paymentService.updateTransactionStatus(txReference, finalSystemStatus, externalId);

            if (updatedTx.isPresent()) {
                PaymentTransaction transaction = updatedTx.get();

                // 2. DOMAIN AUTOMATION SIDE-EFFECTS LOOP
                if (isSuccessful) {
                    // Scenario A: Check if this corresponds to a PENDING_PAYMENT bar tab total
                    billingRepository.findByStatus("PENDING_PAYMENT").stream()
                        .filter(tab -> tab.getTotalAmount().compareTo(transaction.getAmount()) == 0)
                        .findFirst()
                        .ifPresent(tab -> {
                            tab.setStatus("SETTLED");
                            tab.setSettledAt(LocalDateTime.now());
                            tab.setSettledBy("AUTOMATED_WEBHOOK_BOT");
                            billingRepository.save(tab);
                            System.out.println("🤖 WEBHOOK AUTOMATION: Resolved Lounge Bar Tab ID " + tab.getId() + " matching payment value.");
                        });

                    // Scenario B: Optional tracking for website booking placeholders
                    if (transaction.getCustomerPhone() != null) {
                        // Safe placeholder for custom public web booking validation if required later
                        System.out.println("📱 WEBHOOK AUTOMATION: Processing telemetry check for phone: " + transaction.getCustomerPhone());
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