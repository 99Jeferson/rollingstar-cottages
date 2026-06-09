// Save as TabItemRepository.java
package com.rollingstar.cottages.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rollingstar.cottages.model.BillingTab;
import com.rollingstar.cottages.model.TabItem;

@Repository
public interface TabItemRepository extends JpaRepository<TabItem, Long> {
    
    /**
     * Finds a specific ordered item within a particular guest tab.
     * This allows us to increment the item quantity instead of creating duplicate rows.
     */
    TabItem findByBillingTabAndItemId(BillingTab billingTab, Long itemId);
}