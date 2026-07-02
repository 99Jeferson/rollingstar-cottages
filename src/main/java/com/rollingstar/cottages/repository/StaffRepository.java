package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    /**
     * This method must be explicitly declared here so the 
     * StaffController can use it to fetch sorted workspace records.
     */
    List<Staff> findAllByOrderByEmploymentStatusAscFullNameAsc();

    /**
     * Computes active shift headcount directly at the database layer.
     * Adding this removes the compilation error in DashboardController.
     */
    long countByEmploymentStatus(String status);
}