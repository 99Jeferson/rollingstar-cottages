package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.InventoryRestockLedger; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
//  FIX LINE 10: Ensure this says InventoryRestockRepository to match the file name!
public interface InventoryRestockRepository extends JpaRepository<InventoryRestockLedger, Long> {
    // Left clean for your standard restock history database CRUD transactions
}