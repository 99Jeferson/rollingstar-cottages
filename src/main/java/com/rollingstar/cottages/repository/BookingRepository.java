package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Added "IgnoreCase" to the status parameter to catch both 'confirmed' and 'CONFIRMED'
    List<Booking> findByRoomTypeAndRoomNumberAndStatusIgnoreCase(String roomType, Integer roomNumber, String status);
}