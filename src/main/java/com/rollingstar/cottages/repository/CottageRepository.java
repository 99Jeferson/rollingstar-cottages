package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.Cottage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CottageRepository extends JpaRepository<Cottage, String> { // Changed from Object to String
    
    // --- Your Existing Logic ---
    Optional<Cottage> findByCode(String code);
    long countByStatus(String status);

    // --- New Advanced Desktop Filtering Logic ---
    
    // Filter by exact accommodation grade (e.g., "Standard Cottage", "Deluxe Double Room")
    List<Cottage> findByTypeIgnoreCase(String type);
    
    // Find rooms affordable to walk-in guests up to a specific price threshold (UGX)
    List<Cottage> findByRateLessThanEqual(BigDecimal maxPrice);
    
    // Combined filter for your main administrative dashboard grid layout
    List<Cottage> findByTypeAndStatus(String type, String status);
}