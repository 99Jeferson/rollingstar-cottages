package com.rollingstar.cottages.repository;

import com.rollingstar.cottages.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Automatically generates a SELECT query to find matches by credentials
    Optional<User> findByUsernameAndPassword(String username, String password);
}