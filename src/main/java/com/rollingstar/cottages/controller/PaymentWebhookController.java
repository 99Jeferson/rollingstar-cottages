package com.rollingstar.cottages.controller;

import com.rollingstar.cottages.model.PaymentTransaction;
import com.rollingstar.cottages.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Listens for asynchronous transaction status updates sent by the payment aggregator.
     * Path: POST http://localhost:8080/api/v1/payments/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        try {
            // Check if the payload contains the nested data contract fields
            if (!payload.containsKey("event") || !payload.containsKey("data")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload structure");
            }

            String eventType = (String) payload.get("event");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");

            String txReference = (String) data.get("tx_reference");
            String externalId = (String) data.get("transaction_id");
            String gatewayStatus = (String) data.get("status");

            String finalSystemStatus = "FAILED";
            if ("payment.success".equalsIgnoreCase(eventType) || "SUCCESSFUL".equalsIgnoreCase(gatewayStatus)) {
                finalSystemStatus = "SUCCESSFUL";
            }

            // Trigger the internal decoupled service state transition
            Optional<PaymentTransaction> updatedTx = paymentService.updateTransactionStatus(txReference, finalSystemStatus, externalId);

            if (updatedTx.isPresent()) {
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