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
     * Overloaded fallback method to preserve backwards compatibility.
     * Defaults to MOBILE_MONEY and PENDING state for existing features like cottage checkout.
     */
    @Transactional
    public PaymentTransaction initializePayment(BigDecimal amount, String currency, String customerPhone) {
        return initializePayment(amount, currency, customerPhone, "MOBILE_MONEY");
    }

    /**
     * Master initialization method supporting split payment architectures (CASH vs MOBILE_MONEY).
     */
    @Transactional
    public PaymentTransaction initializePayment(BigDecimal amount, String currency, String customerPhone, String paymentMethod) {
        // Generate a clean, unique transaction reference string
        String uniqueRef = "COT-TX-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        String normalizedMethod = (paymentMethod != null) ? paymentMethod.toUpperCase() : "MOBILE_MONEY";
        String initialStatus = "PENDING";
        String externalIdPlaceholder = null;

        // Architectural Business Rule: Cash transactions are instantly finalized
        if ("CASH".equals(normalizedMethod)) {
            initialStatus = "SUCCESSFUL";
            externalIdPlaceholder = "CASH_COUNTER_COLLECTION";
        }

        // Instantiate the record using our structural parameters
        PaymentTransaction transaction = new PaymentTransaction(
                uniqueRef, 
                amount, 
                currency, 
                customerPhone, 
                initialStatus, 
                normalizedMethod
        );

        if (externalIdPlaceholder != null) {
            transaction.setExternalGatewayId(externalIdPlaceholder);
        }

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