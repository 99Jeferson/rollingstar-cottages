package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.Cottage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CottageRepository extends JpaRepository<Cottage, String> {
    // Inherits standard operations: save(), findAll(), findById(), deleteById()
}