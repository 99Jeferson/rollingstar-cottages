package com.rollingstar.cottages.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rollingstar.cottages.model.User; // Make sure this matches your exact User entity path

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // This tells Spring Data JPA to automatically generate the SQL query to find a user by their username
    User findByUsername(String username);
}