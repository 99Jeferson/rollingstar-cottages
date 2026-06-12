package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    // This exact method must exist here for the controller to use it!
    List<Staff> findAllByOrderByEmploymentStatusAscFullNameAsc();
}