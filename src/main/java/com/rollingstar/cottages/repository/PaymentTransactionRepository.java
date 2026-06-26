package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    
    /**
     * Finds a transaction using our unique system reference token.
     * Crucial for verifying details when an external FinTech or Mobile Money 
     * gateway sends back an asynchronous callback webhook.
     */
    Optional<PaymentTransaction> findByTxReference(String txReference);
}