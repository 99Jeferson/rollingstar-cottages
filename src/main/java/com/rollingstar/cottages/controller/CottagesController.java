package com.rollingstar.cottages.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Arrays;
import java.util.List;

@Controller
public class CottagesController {

    // Mock data structure to represent our cottages
    public static class Cottage {
        private String code;
        private String type;
        private double rate;
        private String status; // AVAILABLE, OCCUPIED, MAINTENANCE
        private String guest;

        public Cottage(String code, String type, double rate, String status, String guest) {
            this.code = code;
            this.type = type;
            this.rate = rate;
            this.status = status;
            this.guest = guest;
        }
        
        // Getters (Essential for Thymeleaf reflection properties)
        public String getCode() { return code; }
        public String getType() { return type; }
        public double getRate() { return rate; }
        public String getStatus() { return status; }
        public String getGuest() { return guest; }
    }

    // 🌟 FIX: Direct explicit mapping here aligns perfectly with SecurityConfig and Thymeleaf view resolvers
    @GetMapping("/cottages")
    public String showCottagesDashboard(Model model) {
        List<Cottage> cottageList = Arrays.asList(
            new Cottage("CT-01", "VIP Executive Suite", 350000, "AVAILABLE", null),
            new Cottage("CT-02", "Deluxe Double Room", 250000, "OCCUPIED", "John Mukasa"),
            new Cottage("CT-03", "Standard Honeymoon Cottage", 200000, "AVAILABLE", null),
            new Cottage("CT-04", "VIP Executive Suite", 350000, "MAINTENANCE", null),
            new Cottage("CT-05", "Deluxe Double Room", 250000, "OCCUPIED", "Sarah Namubiru")
        );

        model.addAttribute("cottages", cottageList);
        return "cottages"; // Maps directly to src/main/resources/templates/cottages.html
    }
}