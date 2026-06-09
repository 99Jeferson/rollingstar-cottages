// Save as BillingTabRepository.java
package com.rollingstar.cottages.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.rollingstar.cottages.model.BillingTab;

public interface BillingTabRepository extends JpaRepository<BillingTab, Long> {
    List<BillingTab> findByStatus(String status);
}