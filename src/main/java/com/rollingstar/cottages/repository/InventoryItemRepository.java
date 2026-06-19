package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.stockQuantity <= :threshold")
    long countLowStockItems(@Param("threshold") int threshold);
}