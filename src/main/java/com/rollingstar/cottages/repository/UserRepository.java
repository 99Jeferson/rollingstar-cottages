package com.rollingstar.cottages.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rollingstar.cottages.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Spring Data JPA parses this method signature to automatically build 
     * a secure, parameterized SQL Prepared Statement query:
     * "SELECT * FROM users WHERE username = ?"
     */
    User findByUsername(String username);
}