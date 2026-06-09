// Save as InventoryItemRepository.java
package com.rollingstar.cottages.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.rollingstar.cottages.model.InventoryItem;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {}