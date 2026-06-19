package com.rollingstar.cottages.service;

import com.rollingstar.cottages.model.User; // Assumes your User entity package

public interface UserService {
    User registerNewUser(String username, String plainPassword, String role);
    boolean verifyCredentials(String username, String plainPassword);
}