package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.BillingTab; //  Points to your existing model
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PaymentRepository extends JpaRepository<BillingTab, Long> {

    //  Custom JPQL query to sum total earnings on settled tabs for a specific date
    @Query("SELECT SUM(b.totalAmount) FROM BillingTab b WHERE b.status = 'SETTLED' AND CAST(b.settledAt AS localdate) = :targetDate")
    Long calculateRevenueSumByDate(@Param("targetDate") LocalDate targetDate);
}