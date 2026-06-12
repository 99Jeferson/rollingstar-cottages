package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.BillingTab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<BillingTab, Long> {
    
    // Finds tabs by their specific lifecycle status (e.g., OPEN, SETTLED)
    List<BillingTab> findByStatus(String status);
    
    // Finds a unique tab by its tracking reference sequence
    Optional<BillingTab> findByReferenceId(String referenceId);
    
    // Finds tabs filtered by workspace sector and payment state
    List<BillingTab> findByDepartmentSourceAndStatus(String departmentSource, String status);
    
    // Sorts all entries by their latest updates
    List<BillingTab> findAllByOrderByUpdatedAtDesc();
}