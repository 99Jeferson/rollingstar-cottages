package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.Cottage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CottageRepository extends JpaRepository<Cottage, Object> { 
    
    // Kept from our previous step to clear your prior error
    Optional<Cottage> findByCode(String code);

    //  ADD THIS LINE: Tells Spring Data to run a 'SELECT COUNT(*)' query matching the status property
    long countByStatus(String status);
}