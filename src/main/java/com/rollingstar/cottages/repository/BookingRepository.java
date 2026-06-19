package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByRoomTypeAndRoomNumberAndStatusIgnoreCase(String roomType, int roomNumber, String status);
    
    // Explicitly marks this as a modifying query and ensures a transaction context
    @Modifying
    @Transactional
    void deleteByRoomTypeIgnoreCaseAndRoomNumberAndStatusIgnoreCase(String roomType, int roomNumber, String status);
}