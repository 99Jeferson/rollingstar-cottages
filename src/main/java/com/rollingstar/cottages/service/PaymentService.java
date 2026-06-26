package com.rollingstar.cottages.service;

import com.rollingstar.cottages.model.PaymentTransaction;
import com.rollingstar.cottages.repository.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentTransactionRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentTransactionRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Initializes a new payment transaction record inside our system.
     */
    @Transactional
    public PaymentTransaction initializePayment(BigDecimal amount, String currency, String customerPhone) {
        // Generate a clean, unique transaction reference string using a timestamp and a short UUID hash
        String uniqueRef = "COT-TX-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        // Create a new transaction instance defaulting to "PENDING"
        PaymentTransaction transaction = new PaymentTransaction(uniqueRef, amount, currency, customerPhone, "PENDING");

        // Persist it into our database
        return paymentRepository.save(transaction);
    }

    /**
     * Processes incoming webhook update actions to transition transaction statuses.
     */
    @Transactional
    public Optional<PaymentTransaction> updateTransactionStatus(String txReference, String newStatus, String externalId) {
        return paymentRepository.findByTxReference(txReference)
                .map(transaction -> {
                    transaction.setStatus(newStatus);
                    transaction.setExternalGatewayId(externalId);
                    transaction.setUpdatedAt(LocalDateTime.now());
                    return paymentRepository.save(transaction);
                });
    }
}