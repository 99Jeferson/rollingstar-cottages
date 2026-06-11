package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.InventoryRestockLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryRestockLedgerRepository extends JpaRepository<InventoryRestockLedger, Long> {
    // Fetches restock history ordered cleanly with the latest items first
    List<InventoryRestockLedger> findAllByOrderByRestockedAtDesc();
}